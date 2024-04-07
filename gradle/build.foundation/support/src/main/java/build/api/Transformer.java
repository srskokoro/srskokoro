package build.api;

// Exists as a workaround for https://github.com/gradle/gradle/issues/12388
@FunctionalInterface
public interface Transformer<OUT, IN> extends org.gradle.api.Transformer<OUT, IN> {

	OUT map(IN input);

	@SuppressWarnings("NullableProblems")
	@Override
	default OUT transform(IN in) {
		return map(in);
	}
}
