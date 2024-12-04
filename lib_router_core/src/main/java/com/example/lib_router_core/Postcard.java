package com.example.lib_router_core;

import android.os.Bundle;

import androidx.navigation.NavController;

import com.example.lib_compiler.model.RouteMeta;
import com.example.lib_router_core.callback.NavigationCallback;
import com.example.lib_router_core.template.Router;

/**
 * @Author winiymissl
 * @Date 2024-04-07 21:50
 * @Version 1.0
 */
public class Postcard extends RouteMeta {

    private Bundle bundle;
    private Object tag;             // A tag prepare for some thing wrong. inner params, DO NOT USE!
    private int timeout = 300;      // Navigation timeout, TimeUnit.Second
    private String prerequisiteDestination;
    private String prerequisiteDestinationGraph;

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Postcard(RouteType type, String destinationText, String graphText, Class<?> destination) {
        setType(type);
        setDestinationText(destinationText);
        setGraphText(graphText);
        setDestination(destination);
    }

    public Postcard with(Bundle bundle) {
        if (null != bundle) {
            this.bundle = bundle;
        }
        return this;
    }

    public Object navigation() {
        return Router.getInstance().navigation(null, this, null, false, null);
    }

    public Object navigation(NavController navController) {
        return Router.getInstance().navigation(navController, this, null, false, null);
    }

    public Object navigation(NavController navController, String popUpToDestination, boolean inclusive) {
        return Router.getInstance().navigation(navController, this, popUpToDestination, inclusive, null);
    }

    public Object navigation(NavController navController, NavigationCallback callback) {
        return Router.getInstance().navigation(navController, this, null, false, callback);
    }

    public Object navigation(NavController navController, String popUpToDestination, boolean inclusive, NavigationCallback callback) {
        return Router.getInstance().navigation(navController, this, popUpToDestination, inclusive, callback);
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPrerequisiteDestination() {
        String preValue = prerequisiteDestination;
        prerequisiteDestination = "";
        return preValue;
    }

    public void setPrerequisiteDestination(String prerequisiteDestination) {
        this.prerequisiteDestination = prerequisiteDestination;
    }

    public String getPrerequisiteDestinationGraph() {
        return prerequisiteDestinationGraph;
    }

    public void setPrerequisiteDestinationGraph(String prerequisiteDestinationGraph) {
        this.prerequisiteDestinationGraph = prerequisiteDestinationGraph;
    }
}
