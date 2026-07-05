
namespace EnumHelper {
	/**
	 * Resolves `value` as an enum value of `enumType`.
	 * 
	 * If `value` is not a valid value for `enumType`, `null` is returned.
	 * 
	 * @param enumType The enum to resolve `value` for.
	 * @param value The value to resolve.
	 * @returns The enum value in `enumType`, `null` otherwise.
	 */
	export function enumValueOf<T extends Record<string, number | string>>(enumType: T, value: string | number): T[keyof T] | null {
		value = value.toString();

		// A number is used and said number exists in the enum.
		// Return directly, since numbers are mapped inversely (meaning it is the value itself).
		if (!Number.isNaN(parseInt(value)) && value in enumType) return parseInt(value) as T[keyof T];

		for (const enumKey in enumType) {
			if (!Number.isNaN(parseInt(enumKey))) continue;

			// Strings are not mapped inversely, so this is fine.
			if (value.toLowerCase() === enumKey.toLowerCase()) return enumType[enumKey];

			if (enumType[enumKey].toString().toLowerCase() === value.toLowerCase()) return enumType[enumKey];
		}

		return null;
	}

	/**
	 * Returns all values in an enum.
	 * @param enumType The enum to return values for.
	 * @returns All the values in `enumType`.
	 */
	export function enumValues<T extends Record<string, string | number>>(enumType: T): T[keyof T][] {
		return (Object.keys(enumType) as Extract<keyof T, string>[])
			.filter(key => isNaN(Number(key)))
			.map(key => enumType[key]);
	}
}

export default EnumHelper;