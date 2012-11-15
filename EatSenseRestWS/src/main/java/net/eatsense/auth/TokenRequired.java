/**
 * 
 */
package net.eatsense.auth;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import net.eatsense.auth.AccessToken.TokenType;

/**
 * @author Nils Weiher
 *
 */
@Retention (RUNTIME)
@Target({METHOD})
public @interface TokenRequired {
	TokenType value();
}
