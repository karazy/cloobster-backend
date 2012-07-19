package net.eatsense.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.eatsense.exceptions.ValidationException;

import com.google.inject.Inject;

/**
 * Wraps {@link Validator} methods and throws fitting exception on violation.
 * 
 * @author Nils Weiher
 *
 */
public class ValidationHelper {
	private final Validator validator;
	
	@Inject
	public ValidationHelper(Validator validator) {
		super();
		this.validator = validator;
	}

	/**
	 * Validates an object and throws exception on violation with an error message, containing the property and message of the violation.
	 * 
	 * @param object The object to validate.
	 * @param groups Validation groups.
	 * @return
	 * @throws ValidationException If a violation was found.
	 */
	public <T> Set<ConstraintViolation<T>> validate(T object, Class<?>... groups) throws ValidationException {
		Set<ConstraintViolation<T>> violationSet = validator.validate(object);
		if(!violationSet.isEmpty()) {
			StringBuilder stringBuilder = new StringBuilder("validation errors:");
			for (ConstraintViolation<T> violation : violationSet) {
				stringBuilder.append(String.format(" \"%s\" %s.", violation.getPropertyPath(), violation.getMessage()));
			}
			throw new ValidationException(stringBuilder.toString(), "validationError");
		}
		
		return violationSet;
	}
}
