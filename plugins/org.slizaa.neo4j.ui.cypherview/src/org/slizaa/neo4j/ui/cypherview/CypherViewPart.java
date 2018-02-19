package org.slizaa.neo4j.ui.cypherview;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.slizaa.neo4j.opencypher.ui.internal.OpencypherActivator;
import org.slizaa.neo4j.ui.cypherview.internal.DbAdapterQueryPanel;

import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class CypherViewPart {

  /** - */
  private DbAdapterQueryPanel       _panel;

  /** - */
  private EmbeddedEditorModelAccess _model;

  public String getModel() {
    return this._model.getSerializedModel();
  }

  @PostConstruct
  public void createComposite(Composite parent, MPart mPart) {

    //
    System.out.println("*************** " + mPart);
    MToolBar toolbar = MMenuFactory.INSTANCE.createToolBar();
    // create the tool item programmatically
    MDirectToolItem element = MMenuFactory.INSTANCE.createDirectToolItem();
    element.setElementId("myToolItemId");
    element.setIconURI("platform:/plugin/org.slizaa.neo4j.ui.cypherview/icons/cypherView.png");
    element.setContributionURI(
        "bundleclass://org.slizaa.neo4j.ui.cypherview/org.slizaa.neo4j.ui.cypherview.internal.HandlerOne");
    element.setVisible(true);
    element.setEnabled(true);
    toolbar.getChildren().add(element);
    mPart.setToolbar(toolbar);

    //
    OpencypherActivator activator = OpencypherActivator.getInstance();
    Injector injector = activator.getInjector(OpencypherActivator.ORG_SLIZAA_NEO4J_OPENCYPHER_OPENCYPHER);

    //
    OpenCypherResourceProvider provider = injector.getInstance(OpenCypherResourceProvider.class);
    EmbeddedEditorFactory factory = injector.getInstance(EmbeddedEditorFactory.class);

    //
    this._panel = new DbAdapterQueryPanel(parent, () -> {
      return this._model.getSerializedModel();
    });

    //
    Composite interim = new Composite(this._panel.getEditorArea(), SWT.NONE);
    interim.setLayout(GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 0).margins(0, 0).create());
    EmbeddedEditor editor = factory.newEditor(provider).showErrorAndWarningAnnotations().withParent(interim);
    this._model = editor.createPartialEditor();

    // Allow data to be copied or moved to the drop target
    int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
    DropTarget target = new DropTarget(editor.getViewer().getControl(), operations);

    // Receive data in Text or File format
    final TextTransfer textTransfer = TextTransfer.getInstance();
    final FileTransfer fileTransfer = FileTransfer.getInstance();
    final ResourceTransfer resourceTransfer = ResourceTransfer.getInstance();
    Transfer[] types = new Transfer[] { fileTransfer, textTransfer, resourceTransfer };
    target.setTransfer(types);

    target.addDropListener(new DropTargetListener() {

      @Override
      public void dropAccept(DropTargetEvent event) {
      }

      @Override
      public void drop(DropTargetEvent event) {

        //
        if (!ResourceTransfer.getInstance().isSupportedType(event.currentDataType)
            || !(event.data instanceof IResource[])) {
          event.detail = DND.DROP_NONE;
          return;
        }

        //
        IResource[] resources = (IResource[]) event.data;

        //
        if (resources.length != 1 || resources[0].getType() != IResource.FILE
            || !resources[0].getName().endsWith(".cypher")) {
          event.detail = DND.DROP_NONE;
          return;
        }

        //
        CypherViewPart.this._model.updateModel(getContent((IFile) resources[0]));

        //
        event.detail = DND.DROP_COPY;
        return;
      }

      @Override
      public void dragOver(DropTargetEvent event) {
      }

      @Override
      public void dragOperationChanged(DropTargetEvent event) {
      }

      @Override
      public void dragLeave(DropTargetEvent event) {
      }

      @Override
      public void dragEnter(DropTargetEvent event) {
        if (event.detail == DND.DROP_MOVE || event.detail == DND.DROP_DEFAULT) {
          if ((event.operations & DND.DROP_COPY) != 0) {
            event.detail = DND.DROP_COPY;
          } else {
            event.detail = DND.DROP_NONE;
          }
        }
      }

      private String getContent(IFile resource) {
        try (Scanner scanner = new Scanner(resource.getContents(), StandardCharsets.UTF_8.name())) {
          return scanner.useDelimiter("\\A").next();
        } catch (CoreException e) {
          return "";
        }
      }
    });

    // Configuring default font
    StyledText textWidget = editor.getViewer().getTextWidget();
    textWidget.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

    //
    this._panel.registerGraphDatabaseClientAdapterAwareOSGiService();
  }

  @PreDestroy
  public void dispose() {

    //
    this._panel.unregisterGraphDatabaseClientAdapterAwareOSGiService();
  }
}
