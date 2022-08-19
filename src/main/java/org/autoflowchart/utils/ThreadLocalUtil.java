package org.autoflowchart.utils;

import org.autoflowchart.objects.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oabern
 * @date 2022/8/19
 */
public class ThreadLocalUtil {
    public static final ThreadLocal<Map<String, Object>> tl = new ThreadLocal<>();
    static {
        Map<String, Object> map = new HashMap<>();
        tl.set(map);
    }

    public static final String RETURN_NODE = "Return_Node";

    public ThreadLocalUtil() {
    }

    public static void setReturnNode(Node node) {
        if(node == null) throw new RuntimeException("cannot put null return node!");
        tl.get().put(RETURN_NODE, node);
    }

    public static Node getReturnNode() {
        Object o = tl.get().get(RETURN_NODE);
//        if(o == null) throw new RuntimeException("return node is null");
        return (Node) o;
    }
}
