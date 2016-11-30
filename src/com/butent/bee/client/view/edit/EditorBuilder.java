package com.butent.bee.client.view.edit;

import com.butent.bee.shared.ui.EditorDescription;

public interface EditorBuilder {

  void afterCreateEditor(String source, Editor editor, boolean embedded);

  Editor maybeCreateEditor(String source, EditorDescription editorDescription, boolean embedded);
}
