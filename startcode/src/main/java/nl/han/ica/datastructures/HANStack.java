package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.List;

public class HANStack<T> implements IHANStack<T> {
    private final List<T> stack;

    public HANStack() {
        stack = new ArrayList<T>();
    }

    @Override
    public void push(T value) {
        if (value != null) {
            stack.add(value);
        }
    }

    @Override
    public T pop() {
        if (!stack.isEmpty()) {
            return stack.remove(stack.size() - 1);
        }
        return null;
    }

    @Override
    public T peek() {
        if (!stack.isEmpty()) {
            return stack.get(stack.size() - 1);
        }
        return null;

    }
}
