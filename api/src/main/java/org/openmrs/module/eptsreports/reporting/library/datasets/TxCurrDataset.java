/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.eptsreports.reporting.library.datasets;

import java.util.ArrayList;

import org.openmrs.module.eptsreports.metadata.HivMetadata;
import org.openmrs.module.eptsreports.reporting.library.cohorts.AgeCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenericCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.GenderCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.cohorts.TxCurrCohortQueries;
import org.openmrs.module.eptsreports.reporting.library.indicators.HivIndicators;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TxCurrDataset extends BaseDataSet {
	
	@Autowired
	private AgeCohortQueries ageCohortQueries;
	
	@Autowired
	private GenderCohortQueries genderCohortQueries;
	
	@Autowired
	private TxCurrCohortQueries txCurrCohortQueries;
	
	@Autowired
	private GenericCohortQueries genericCohortQueries;
	
	@Autowired
	private HivIndicators hivIndicators;
	
	@Autowired
	private HivMetadata hivMetadata;
	
	public CohortIndicatorDataSetDefinition constructTxNewDatset() {
		
		CohortIndicatorDataSetDefinition dataSetDefinition = new CohortIndicatorDataSetDefinition();
		dataSetDefinition.setName("TX_CURR Data Set");
		dataSetDefinition.addParameters(getParameters());
		
		/*
		 * Looks for patients enrolled in ART program (program 2=SERVICO TARV -
		 * TRATAMENTO) before or on end date
		 */
		CohortDefinition enrolledBeforeEndDate = genericCohortQueries.createInProgram("InARTProgram", hivMetadata.getARTProgram());
		
		/*
		 * Looks for patients registered as START DRUGS (answer to question 1255 = ARV
		 * PLAN is 1256 = START DRUGS) in the first drug pickup (encounter type
		 * 18=S.TARV: FARMACIA) or follow up consultation for adults and children
		 * (encounter types 6=S.TARV: ADULTO SEGUIMENTO and 9=S.TARV: PEDIATRIA
		 * SEGUIMENTO) before or on end date
		 */
		CohortDefinition patientWithSTARTDRUGSObs = txCurrCohortQueries.getPatientWithSTARTDRUGSObsBeforeOrOnEndDate();
		
		/*
		 * Looks for with START DATE (Concept 1190=HISTORICAL DRUG START DATE) filled in
		 * drug pickup (encounter type 18=S.TARV: FARMACIA) or follow up consultation
		 * for adults and children (encounter types 6=S.TARV: ADULTO SEGUIMENTO and
		 * 9=S.TARV: PEDIATRIA SEGUIMENTO) where START DATE is before or equal end date
		 */
		CohortDefinition patientWithHistoricalDrugStartDateObs = txCurrCohortQueries
		        .getPatientWithHistoricalDrugStartDateObsBeforeOrOnEndDate();
		
		// Looks for patients who had at least one drug pick up (encounter type
		// 18=S.TARV: FARMACIA) before end date
		CohortDefinition patientsWithDrugPickUpEncounters = txCurrCohortQueries
		        .getPatientWithFirstDrugPickupEncounterBeforeOrOnEndDate();
		
		// Looks for patients enrolled on art program (program 2 - SERVICO TARV -
		// TRATAMENTO) who left ART program
		SqlCohortDefinition patientsWhoLeftARTProgramBeforeOrOnEndDate = txCurrCohortQueries
		        .getPatientsWhoLeftARTProgramBeforeOrOnEndDate();
		
		// Looks for patients that from the date scheduled for next drug pickup (concept
		// 5096=RETURN VISIT DATE FOR ARV DRUG) until end date have completed 60 days
		// and have not returned
		SqlCohortDefinition patientsWhoHaveNotReturned = txCurrCohortQueries.getPatientsWhoHaveNotReturned();
		
		// Looks for patients that from the date scheduled for next follow up
		// consultation (concept 1410=RETURN VISIT DATE) until the end date have not
		// completed 60 days
		SqlCohortDefinition patientsWhoHaveNotCompleted60Days = txCurrCohortQueries.getPatientsWhoHaveNotCompleted60Days();
		
		// Looks for patients that were registered as abandonment (program workflow
		// state is 9=ABANDONED) but from the date scheduled for next drug pick up
		// (concept 5096=RETURN VISIT DATE FOR ARV DRUG) until the end date have not
		// completed 60 days
		SqlCohortDefinition abandonedButHaveNotcompleted60Days = txCurrCohortQueries.getAbandonedButHaveNotcompleted60Days();
		
		CohortDefinition males = genderCohortQueries.MaleCohort();
		
		CohortDefinition females = genderCohortQueries.FemaleCohort();
		
		CohortDefinition PatientBelow1Year = ageCohortQueries.createBelowYAgeCohort("PatientBelow1Year", 1);
		CohortDefinition PatientBetween1And9Years = ageCohortQueries.createXtoYAgeCohort("PatientBetween1And9Years", 1, 9);
		
		ArrayList<CohortDefinition> agesRange = new ArrayList<CohortDefinition>();
		agesRange.add(ageCohortQueries.createXtoYAgeCohort("PatientBetween10And14Years", 10, 14));
		agesRange.add(ageCohortQueries.createXtoYAgeCohort("PatientBetween15And19Years", 15, 19));
		agesRange.add(ageCohortQueries.createXtoYAgeCohort("PatientBetween20And24Years", 20, 24));
		agesRange.add(ageCohortQueries.createXtoYAgeCohort("PatientBetween25And29Years", 25, 29));
		agesRange.add(ageCohortQueries.createXtoYAgeCohort("PatientBetween30And34Years", 30, 34));
		agesRange.add(ageCohortQueries.createXtoYAgeCohort("PatientBetween35And39Years", 35, 39));
		agesRange.add(ageCohortQueries.createXtoYAgeCohort("PatientBetween40And49Years", 40, 49));
		agesRange.add(ageCohortQueries.createOverXAgeCohort("PatientBetween50YearsAndAbove", 50));
		
		// Male and Female <1
		CohortDefinition patientBellowOneYearCurrentlyInART = txCurrCohortQueries.getTxCurrCompositionCohort(
		    "patientBellowOneYearCurrentlyInART", enrolledBeforeEndDate, patientWithSTARTDRUGSObs,
		    patientWithHistoricalDrugStartDateObs, patientsWithDrugPickUpEncounters, patientsWhoLeftARTProgramBeforeOrOnEndDate,
		    patientsWhoHaveNotReturned, patientsWhoHaveNotCompleted60Days, abandonedButHaveNotcompleted60Days, PatientBelow1Year,
		    null);
		CohortIndicator patientBelow1YearCurrentlyInARTIndicator = hivIndicators
		        .patientBelow1YearEnrolledInHIVStartedARTIndicatorBeforeOrOnEndDate(patientBellowOneYearCurrentlyInART);
		dataSetDefinition.addColumn("C1<1", "TX_CURR: Currently on ART: Patients below 1 year",
		    new Mapped<CohortIndicator>(patientBelow1YearCurrentlyInARTIndicator,
		            ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}")),
		    "");
		
		// Male and Female between 1 and 9 years
		CohortDefinition patientBetween1And9YearsCurrentlyInART = txCurrCohortQueries.getTxCurrCompositionCohort(
		    "patientBetween1And9YearsCurrentlyInART", enrolledBeforeEndDate, patientWithSTARTDRUGSObs,
		    patientWithHistoricalDrugStartDateObs, patientsWithDrugPickUpEncounters, patientsWhoLeftARTProgramBeforeOrOnEndDate,
		    patientsWhoHaveNotReturned, patientsWhoHaveNotCompleted60Days, abandonedButHaveNotcompleted60Days,
		    PatientBetween1And9Years, null);
		CohortIndicator patientBetween1And9YearsCurrentlyInARTIndicator = hivIndicators
		        .patientBetween1And9YearsEnrolledInHIVStartedARTIndicatorBeforeOrOnEndDate(patientBetween1And9YearsCurrentlyInART);
		dataSetDefinition.addColumn("C119", "TX_CURR: Currently on ART: Patients between 1 and 9 years",
		    new Mapped<CohortIndicator>(patientBetween1And9YearsCurrentlyInARTIndicator,
		            ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}")),
		    "");
		
		// Male
		int i = 2;
		for (CohortDefinition ageCohort : agesRange) {
			CohortDefinition patientInYearRange = txCurrCohortQueries.getTxCurrCompositionCohort("patientEnrolledInARTStartedMales",
			    enrolledBeforeEndDate, patientWithSTARTDRUGSObs, patientWithHistoricalDrugStartDateObs,
			    patientsWithDrugPickUpEncounters, patientsWhoLeftARTProgramBeforeOrOnEndDate, patientsWhoHaveNotReturned,
			    patientsWhoHaveNotCompleted60Days, abandonedButHaveNotcompleted60Days, ageCohort, males);
			CohortIndicator patientInYearRangeCurrenltyInHIVStartedARTIndicator = hivIndicators
			        .patientInYearRangeEnrolledInHIVStartedARTIndicatorBeforeOrOnEndDate(patientInYearRange);
			dataSetDefinition.addColumn("C1M" + i, "Males:TX_CURR: Currently on ART by age and sex: " + ageCohort.getName(),
			    new Mapped<CohortIndicator>(patientInYearRangeCurrenltyInHIVStartedARTIndicator,
			            ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}")),
			    "");
			
			i++;
		}
		
		// Females
		int j = 2;
		for (CohortDefinition ageCohort : agesRange) {
			CohortDefinition patientInYearRange = txCurrCohortQueries.getTxCurrCompositionCohort("patientEnrolledInARTStartedFemales",
			    enrolledBeforeEndDate, patientWithSTARTDRUGSObs, patientWithHistoricalDrugStartDateObs,
			    patientsWithDrugPickUpEncounters, patientsWhoLeftARTProgramBeforeOrOnEndDate, patientsWhoHaveNotReturned,
			    patientsWhoHaveNotCompleted60Days, abandonedButHaveNotcompleted60Days, ageCohort, females);
			CohortIndicator patientInYearRangeCurrenltyInHIVStartedARTIndicator = hivIndicators
			        .patientInYearRangeEnrolledInHIVStartedARTIndicatorBeforeOrOnEndDate(patientInYearRange);
			
			dataSetDefinition.addColumn("C1F" + j, "Females:TX_CURR: Currently on ART by age and sex: " + ageCohort.getName(),
			    new Mapped<CohortIndicator>(patientInYearRangeCurrenltyInHIVStartedARTIndicator,
			            ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}")),
			    "");
			j++;
		}
		
		CohortDefinition allPatientsCurrentlyInART = txCurrCohortQueries.getTxCurrCompositionCohort("allPatientsCurrentlyInART",
		    enrolledBeforeEndDate, patientWithSTARTDRUGSObs, patientWithHistoricalDrugStartDateObs, patientsWithDrugPickUpEncounters,
		    patientsWhoLeftARTProgramBeforeOrOnEndDate, patientsWhoHaveNotReturned, patientsWhoHaveNotCompleted60Days,
		    abandonedButHaveNotcompleted60Days, null, null);
		CohortIndicator allPatientsCurrentlyInARTARTIndicator = hivIndicators
		        .patientEnrolledInHIVStartedARTIndicatorBeforeOrOnEndDate(allPatientsCurrentlyInART);
		dataSetDefinition.addColumn("C1All", "TX_CURR: Currently on ART",
		    new Mapped<CohortIndicator>(allPatientsCurrentlyInARTARTIndicator,
		            ParameterizableUtil.createParameterMappings("endDate=${endDate},location=${location}")),
		    "");
		
		return dataSetDefinition;
	}
}