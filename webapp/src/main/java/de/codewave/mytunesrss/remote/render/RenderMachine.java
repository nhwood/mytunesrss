package de.codewave.mytunesrss.remote.render;

import de.codewave.utils.sql.*;
import de.codewave.mytunesrss.remote.service.*;

import java.util.*;

/**
 * de.codewave.mytunesrss.remote.render.RenderMachine
 */
public class RenderMachine {
    private static final Renderer SELF_RENDERER = new Renderer() {
        public Object render(Object o) {
            return o;
        }
    };

    private Map<Class, Renderer> myRenderers = new HashMap<Class, Renderer>();

    public RenderMachine() {
        addRenderer(Integer.class, SELF_RENDERER);
        addRenderer(Integer.TYPE, SELF_RENDERER);
        addRenderer(Boolean.class, SELF_RENDERER);
        addRenderer(Boolean.TYPE, SELF_RENDERER);
        addRenderer(String.class, SELF_RENDERER);
        addRenderer(Double.class, SELF_RENDERER);
        addRenderer(Double.TYPE, SELF_RENDERER);
        addRenderer(Date.class, SELF_RENDERER);
        addRenderer(byte[].class, SELF_RENDERER);
        addRenderer(Map.class, new MapRenderer());
        addRenderer(Object[].class, new ArrayRenderer());
        addRenderer(List.class, new ListRenderer());
        addRenderer(DataStoreQuery.QueryResult.class, new QueryResultRenderer());
        addRenderer(QueryResultWrapper.class, new QueryResultWrapperRenderer());
    }

    public void addRenderer(Class type, Renderer renderer) {
        myRenderers.put(type, renderer);
    }

    public Renderer getRendererForClass(Class type) {
        return myRenderers.get(type);
    }

    public Renderer getRendererForObject(Object o) {
        return getRendererForClass(o.getClass());
    }

    public Object render(Object o) {
        if (o != null) {
            Renderer renderer = getRendererForObject(o);
            if (renderer != null) {
                return renderer.render(o);
            } else {
                throw new IllegalArgumentException("No renderer for type \"" + o.getClass() + "\" found.");
            }
        } else {
            return "";
        }
    }

    private class ListRenderer implements Renderer<List, List> {
        public List render(List input) {
            List output = new ArrayList(input.size());
            for (Object item : input) {
                output.add(RenderMachine.this.render(item));
            }
            return output;
        }
    }

    private class MapRenderer implements Renderer<Map, Map> {
        public Map render(Map input) {
            Map output = new HashMap(input.size());
            for (Object o : input.entrySet()) {
                Map.Entry entry = (Map.Entry)o;
                output.put(RenderMachine.this.render(entry.getKey()), RenderMachine.this.render(entry.getValue()));
            }
            return output;
        }
    }

    private class ArrayRenderer implements Renderer<Object[], Object[]> {
        public Object[] render(Object[] input) {
            Object[] output = new Object[input.length];
            for (int i = 0; i < input.length; i++) {
                output[i] = RenderMachine.this.render(input[i]);
            }
            return output;
        }
    }

    private class QueryResultRenderer implements Renderer<List, DataStoreQuery.QueryResult> {
        public List render(DataStoreQuery.QueryResult input) {
            List output = new ArrayList(input.getResultSize());
            for (Object item = input.nextResult(); item != null; item = input.nextResult()) {
                output.add(RenderMachine.this.render(item));
            }
            return output;
        }
    }

    private class QueryResultWrapperRenderer implements Renderer<List, QueryResultWrapper> {
        public List render(QueryResultWrapper input) {
            List output = new ArrayList(input.getResultSize());
            for (Object item = input.nextResult(); item != null; item = input.nextResult()) {
                output.add(RenderMachine.this.render(item));
            }
            return output;
        }
    }
}