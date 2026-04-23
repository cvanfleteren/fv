package be.iffy.fv.dsl;

import lombok.SneakyThrows;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

class PropertySelectorSupport {

    private static final ThreadLocal<PropertySelector<?, ?>> SELECTOR_HOLDER = new ThreadLocal<>();

    // use a ClassValue cache, so we can't leak / hold on to classes unnecessarily
    private static final ClassValue<String> NAME_CACHE = new ClassValue<>() {
        @Override
        protected String computeValue(Class<?> type) {
            PropertySelector<?, ?> selector = SELECTOR_HOLDER.get();
            String methodName = serialized(selector).getImplMethodName();
            return processName(methodName);
        }
    };

    /**
     * Extracts the methopdname from the {@link PropertySelector} and converts it into a property name.
     * Handles getXxx and isXxx prefixes by stripping them and converting the next character to lowercase if necessary
     */
    public static String getImplMethodName(PropertySelector<?, ?> selector) {
        try {
            // we can't pass the PropertySelector properly, so set it as a ThreadLocal
            SELECTOR_HOLDER.set(selector);
            return NAME_CACHE.get(selector.getClass());
        } finally {
            SELECTOR_HOLDER.remove();
        }
    }

    @SneakyThrows
    static SerializedLambda serialized(PropertySelector<?, ?> selector) {
        Method method = selector.getClass().getDeclaredMethod("writeReplace");
        method.setAccessible(true);
        return (SerializedLambda) method.invoke(selector);
    }

    private static String processName(String methodName) {
        int offset = 0;
        int length = methodName.length();
        if (methodName.startsWith("get") && length > 3 && Character.isUpperCase(methodName.charAt(3))) {
            offset = 3;
        } else if (methodName.startsWith("is") && length > 2 && Character.isUpperCase(methodName.charAt(2))) {
            offset = 2;
        }

        // record component
        if (offset == 0) {
            return methodName;
        }

        //e.g. getURL -> URL
        if (length > offset + 1 && Character.isUpperCase(methodName.charAt(offset + 1)) && Character.isUpperCase(methodName.charAt(offset))) {
            return methodName.substring(offset);
        }

        char firstChar = methodName.charAt(offset);
        char lowerFirst = Character.toLowerCase(firstChar);
        if (firstChar == lowerFirst) {
            return methodName.substring(offset);
        }

        return lowerFirst + methodName.substring(offset + 1);
    }
}
