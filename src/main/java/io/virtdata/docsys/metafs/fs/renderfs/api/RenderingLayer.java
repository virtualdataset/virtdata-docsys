package io.virtdata.docsys.metafs.fs.renderfs.api;

import io.virtdata.docsys.metafs.fs.renderfs.model.TargetPathView;

/**
 * A rendering layer captures the template, renderer, and focal points that
 * are needed to render a specific layer.
 */
public class RenderingLayer extends Ren{
    private TargetPathView contextObject;
    private Renderable renderable;

    public RenderingLayer(TargetPathView contextObject, Renderable renderable) {
        this.contextObject = contextObject;
        this.renderable = renderable;
    }
}
