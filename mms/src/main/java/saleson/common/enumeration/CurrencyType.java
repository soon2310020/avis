package saleson.common.enumeration;

import saleson.common.enumeration.mapper.CodeMapperType;

public enum CurrencyType implements CodeMapperType {
	USD("$", "United States Dollar", true), //
	EUR("€", "Euro", true), //
	JPY("¥", "Japanese yen", true), //
	GBP("£", "Pound sterling", true), //
	AUD("A$", "Australian Dollar", true), //
	CAD("C$", "Canadian Dollar", true), //
	CHF("Fr", "Swiss franc", true), //
	CNY("元", "Renminbi", true), //
	SEK("kr", "Swedish krona", true), //
	NZD("NZ$", "New Zealand Dollar", true), //
	MXN("$", "Mexican peso", true), //
	SGD("S$", "Singapore Dollar", true), //
	HKD("HK$", "Hong Kong Dollar", true), //
	NOK("kr", "Norwegian krone", true), //
	KRW("₩", "South Korean won", true), //
	TRY("₺", "Turkish lira", true), //
	RUB("₽", "Russian ruble", true), //
	INR("₹", "Indian rupee", true), //
	BRL("R$", "Brazilian real", true), //
	ZAR("R", "South African rand", true), //
//	RM("RM", "Malaysian Ringgit", false),//
	MYR("RM", "Malaysian Ringgit", true);

	private String title;
	private String description;
	private boolean enabled;

	CurrencyType(String title, String description, boolean enabled) {
		this.title = title;
		this.description = description;
		this.enabled = enabled;
	}

	@Override
	public String getCode() {
		return name();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Boolean isEnabled() {
		return enabled;
	}

	public static CurrencyType asMyEnum(String str) {
		for (CurrencyType me : CurrencyType.values()) {
			if (me.name().equalsIgnoreCase(str))
				return me;
		}
		return null;
	}
}
