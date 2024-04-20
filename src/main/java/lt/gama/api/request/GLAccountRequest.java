package lt.gama.api.request;

import lt.gama.model.dto.entities.GLAccountDto;

public class GLAccountRequest {

	private GLAccountDto model;

	private GLAccountDto parent;

	// generated

	public GLAccountDto getModel() {
		return model;
	}

	public void setModel(GLAccountDto model) {
		this.model = model;
	}

	public GLAccountDto getParent() {
		return parent;
	}

	public void setParent(GLAccountDto parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "GLAccountRequest{" +
				"model=" + model +
				", parent=" + parent +
				'}';
	}
}
