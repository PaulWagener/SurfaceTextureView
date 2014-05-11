package nl.paulwagener;

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.view.View;

/**
 * A wrapper around GLSurfaceView so that it implements the GLView interface
 * Shouldn't be interesting to use this class outside of this package.
 */
class GLSurfaceViewWrapper implements GLView {

	private final GLSurfaceView glSurfaceView;

	public GLSurfaceViewWrapper(GLSurfaceView glSurfaceView) {
		this.glSurfaceView = glSurfaceView;
	}

	@Override
	public void queueEvent(Runnable r) {
		glSurfaceView.queueEvent(r);
	}

	@Override
	public void setRenderer(Renderer renderer) {
		glSurfaceView.setRenderer(renderer);
	}

	@Override
	public View getView() {
		return glSurfaceView;
	}

}
