package at.bestsolution.efxclipse.runtime.workbench.renderers.base;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler.Save;

import at.bestsolution.efxclipse.runtime.workbench.renderers.base.widget.WLayoutedWidget;
import at.bestsolution.efxclipse.runtime.workbench.renderers.base.widget.WWindow;


@SuppressWarnings("restriction")
public abstract class BaseWindowRenderer<N> extends BaseRenderer<MWindow,WWindow<N>> {
	@PostConstruct
	void init(IEventBroker eventBroker) {
		registerEventListener(eventBroker, UIEvents.Window.TOPIC_X);
		registerEventListener(eventBroker, UIEvents.Window.TOPIC_Y);
		registerEventListener(eventBroker, UIEvents.Window.TOPIC_WIDTH);
		registerEventListener(eventBroker, UIEvents.Window.TOPIC_HEIGHT);
		registerEventListener(eventBroker, UIEvents.UILabel.TOPIC_LABEL);
		registerEventListener(eventBroker, UIEvents.UILabel.TOPIC_TOOLTIP);
	}
	
	@Override
	protected void initWidget(final MWindow element, final WWindow<N> widget) {
		getModelContext(element).set(ISaveHandler.class, new ISaveHandler() {
			
			@Override
			public Save[] promptToSave(Collection<MPart> dirtyParts) {
				return BaseWindowRenderer.this.promptToSave(element, dirtyParts, widget);
			}
			
			@Override
			public Save promptToSave(MPart dirtyPart) {
				return BaseWindowRenderer.this.promptToSave(element, dirtyPart, widget);
//				Collection<MPart> c = Collections.singleton(dirtyPart);
//				return BaseWindowRenderer.this.promptToSave(resourceUtilities,c, widget)[0];
			}
		});
	}
	
	protected abstract Save[] promptToSave(MWindow element, Collection<MPart> dirtyParts, WWindow<N> widget);
	protected abstract Save promptToSave(MWindow element, MPart dirtyPart, WWindow<N> widget);

	@Override
	public void doProcessContent(MWindow element) {
		WWindow<N> windowWidget = getWidget(element);
		
		if (element.getMainMenu() != null) {
			WLayoutedWidget<MMenu> menuWidget = engineCreateWidget(element.getMainMenu());
			if( menuWidget != null ) {
				windowWidget.setMainMenu(menuWidget);	
			}
		}
		
		if( element instanceof MTrimmedWindow ) {
			for( MTrimBar tm : ((MTrimmedWindow)element).getTrimBars() ) {
				WLayoutedWidget<MTrimBar> trimWidget = engineCreateWidget(tm);
				if( trimWidget != null ) {
					trimWidget.addStyleClasses(tm.getSide().name());
					switch (tm.getSide()) {
					case TOP:
						windowWidget.setTopTrim(trimWidget);
						break;
					case RIGHT:
						windowWidget.setRightTrim(trimWidget);
						break;
					case BOTTOM:
						windowWidget.setBottomTrim(trimWidget);
						break;
					case LEFT:
						windowWidget.setLeftTrim(trimWidget);
						break;
					default:
						break;
					}					
				}
			}
		}
		
		for( MWindowElement e : element.getChildren() ) {
			WLayoutedWidget<MWindowElement> widget = engineCreateWidget(e);
			if( widget != null ) {
				windowWidget.addChild(widget);
			}
		}
	}

	@Override
	public void postProcess(MWindow element) {
		super.postProcess(element);
		if( element.isVisible() ) {
			WWindow<N> window = getWidget(element);
			if( window != null ) {
				window.show();
			}
		}
	}

	@Override
	public void childRendered(MWindow parentElement, MUIElement element) {
	}
	
	@Override
	public void hideChild(MWindow container, MUIElement changedObj) {
	}
}
