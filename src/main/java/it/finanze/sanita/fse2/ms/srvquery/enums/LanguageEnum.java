package it.finanze.sanita.fse2.ms.srvquery.enums;

import lombok.Getter;

@Getter
public enum LanguageEnum {
	ARABIC("ar", "Arabic"),
	AZERBAIJANI("az", "Azerbaijani"),
	CATALAN("ca", "Catalan"),
	CHINESE("zh", "Chinese"),
	CZECH("cs", "Czech"),
	DANISH("da", "Danish"),
	DUTCH("nl", "Dutch"),
	ENGLISH("en", "English"),
	ESPERANTO("eo", "Esperanto"),
	FINNISH("fi", "Finnish"),
	FRENCH("fr", "French"),
	GERMAN("de", "German"),
	GREEK("el", "Greek"),
	HEBREW("he", "Hebrew"),
	HINDI("hi", "Hindi"),
	HUNGARIAN("hu", "Hungarian"),
	INDONESIAN("id", "Indonesian"),
	IRISH("ga", "Irish"),
	ITALIAN("it", "Italian"),
	JAPANESE("ja", "Japanese"),
	KOREAN("ko", "Korean"),
	PERSIAN("fa", "Persian"),
	POLISH("pl", "Polish"),
	PORTUGUESE("pt", "Portuguese"),
	RUSSIAN("ru", "Russian"),
	SLOVAK("sk", "Slovak"),
	SPANISH("es", "Spanish"),
	SWEDISH("sv", "Swedish"),
	THAI("th", "Thai"),
	TURKISH("tr", "Turkish"),
	UKRANIAN("uk", "Ukrainian");
	
	private String code;
	private String description;
	
	private LanguageEnum(String inCode, String inDescription) {
		code = inCode;
		description = inDescription;
	}
}
