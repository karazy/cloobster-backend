package net.eatsense.domain.embedded;

/**
 * Options how to use the price value of a product choice
 * @author Nils Weiher
 *
 */
public enum ChoiceOverridePrice {
	NONE, // all choices and products count with their own price
	OVERRIDE_SINGLE_PRICE, // each product/option uses the price of the choice, ignore individual prices
	OVERRIDE_FIXED_SUM // the choice always has a fixed price, ignore individual prices
}
