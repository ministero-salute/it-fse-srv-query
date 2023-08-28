package it.finanze.sanita.fse2.ms.srvquery.utility;

import java.util.Comparator;

import org.hl7.fhir.r4.model.ValueSet;

public class ValueSetComparator {

	public static Comparator<ValueSet> init() {
		return Comparator.comparingLong((ValueSet v) -> v.getIdElement().getIdPartAsLong()).thenComparingLong(v -> v.getMeta().getVersionIdElement().getIdPartAsLong());
	}
}
