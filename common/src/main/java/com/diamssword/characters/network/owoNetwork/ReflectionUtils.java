package com.diamssword.characters.network.owoNetwork;

import org.jetbrains.annotations.ApiStatus;


@ApiStatus.Experimental
public final class ReflectionUtils {

    private ReflectionUtils() {}

    /**
     * Tries to acquire the name of the calling class,
     * {@code depth} frames up the call stack
     *
     * @param depth How many frames upwards to walk the call stack
     * @return The name of the class at {@code depth} in the call stack or
     * {@code <unknown>} if the class name was not found
     */
    public static String getCallingClassName(int depth) {
        return StackWalker.getInstance().walk(s -> s
                .skip(depth)
                .map(StackWalker.StackFrame::getClassName)
                .findFirst()).orElse("<unknown>");
    }
}
