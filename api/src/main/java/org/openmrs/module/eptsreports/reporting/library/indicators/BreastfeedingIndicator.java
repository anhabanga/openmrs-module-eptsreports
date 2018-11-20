package org.openmrs.module.eptsreports.reporting.library.indicators;

import org.openmrs.module.eptsreports.reporting.library.cohorts.CompositionCohortQueries;
import org.openmrs.module.eptsreports.reporting.utils.EptsReportUtils;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BreastfeedingIndicator extends BaseIndicators {
	
	@Autowired
	private CompositionCohortQueries ccq;
	
	/**
	 * Breastfeeding women with viral load suppression in the last 12 months to a common file for reuse
	 * 
	 * @return CohortIndicator
	 */
	public CohortIndicator getBreastfeedingWomenWithSuppressedViralLoadIn12Months() {
		return newCohortIndicator("pregnantWomenWithViralLoadSuppression",
		    EptsReportUtils.map(ccq.breastfeedingWomenAndHasViralLoadSuppressionInTheLast12MonthsNumerator(),
		        "startDate=${startDate},endDate=${endDate},location=${location}"));
	}
	
	/**
	 * Breastfeeding women with viral load in the last 12 months to a common file for reuse
	 * 
	 * @return CohortIndicator
	 */
	public CohortIndicator getBreastfeedingWomenWithViralLoadIn12Months() {
		return newCohortIndicator("pregnantWomenWithViralLoad",
		    EptsReportUtils.map(ccq.breastfeedingWomenAndHasViralLoadInTheLast12MonthsDenominator(),
		        "startDate=${startDate},endDate=${endDate},location=${location}"));
	}
}
