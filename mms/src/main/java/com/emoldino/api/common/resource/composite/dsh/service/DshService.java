package com.emoldino.api.common.resource.composite.dsh.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.common.resource.base.menu.dto.MenuTreeNode;
import com.emoldino.api.common.resource.base.menu.service.MenuService;
import com.emoldino.api.common.resource.base.option.service.OptionService;
import com.emoldino.api.common.resource.base.option.util.OptionUtils;
import com.emoldino.api.common.resource.composite.dsh.dto.DshConfigsGetOut;
import com.emoldino.api.common.resource.composite.dsh.dto.DshContent;
import com.emoldino.api.common.resource.composite.dsh.dto.DshPosition;
import com.emoldino.api.common.resource.composite.dsh.dto.DshWidget;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.MessageUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Transactional
public class DshService {

	public ListOut<DshWidget> get() {
		DshConfigsGetOut reqout = getConfigs();
		return new ListOut<>(reqout.getContentEnabled());
	}

//	private static final Map<String, DshWidget> WIDGETS = new MapBuilder<String, DshWidget>()//
//			.put("WGT-TOL", new DshWidget("WGT-TOL", "Total Toolings", 1, 1))//
//			.put("WGT-TOL-OPR", new DshWidget("WGT-TOL-OPR", "Operational Summary", 1, 1))//
//			.put("WGT-TOL-UTL", new DshWidget("WGT-TOL-UTL", "Overall Utilization", 1, 1))//
//			.put("WGT-TOL-INA", new DshWidget("WGT-TOL-INA", "Inactive Toolings", 1, 1))//
////			.put("W05", new DshWidget("W05", "Widget 5", 2, 1))//
////			.put("W06", new DshWidget("W06", "Widget 6", 2, 1))//
////			.put("W07", new DshWidget("W07", "Widget 7", 2, 1))//
////			.put("W08", new DshWidget("W08", "Widget 8", 3, 1))//
////			.put("W09", new DshWidget("W09", "Widget 9", 3, 1))//
////			.put("W10", new DshWidget("W10", "Widget 10", 4, 1))//
////			.put("W11", new DshWidget("W11", "Widget 11", 4, 1))//
//			.build();
//
//	private static Map<String, DshWidget> getWidgetMap() {
//		return WIDGETS;
//	}

	public DshConfigsGetOut getConfigs() {
		MenuTreeNode dashboard = BeanUtils.get(MenuService.class).getPermitted("CM9021");
		Map<String, DshWidget> map = new LinkedHashMap<>();
		ListOut<DshWidget> defaultWidgets = new ListOut<>();
		int[] i = { 0 };
		if (dashboard != null) {
			dashboard.getChildren().forEach(menu -> {
				DshWidget widget = new DshWidget(//
						menu.getId(), //
						MessageUtils.get(menu.getMessage(), menu.getName(), null), //
						menu.getWidth(), //
						menu.getHeight()//
				);
				map.put(menu.getId(), widget);
				if (i[0]++ < 8) {
					defaultWidgets.add(widget);
				}
			});
		}
		DshConfigsGetOut output = new DshConfigsGetOut();

		// Content
		output.setContent(new ArrayList<>(map.values()));

		// Content Enabled
		{
			List<DshWidget> rlist = new ArrayList<>();
			OptionUtils.getUserContent(//
					"DSH", //
					new TypeReference<ListOut<DshWidget>>() {
					}, //
					defaultWidgets//
			).getContent().forEach(item -> {
				DshWidget widget = map.get(item.getId());
				if (widget == null) {
					rlist.add(item);
					return;
				}
				ValueUtils.map(widget, item);
				output.addEnabled(item);
			});
			output.getContentEnabled().removeAll(rlist);
		}

		return output;
	}

	public void postConfigs(ListIn<DshWidget> input) {
		BeanUtils.get(OptionService.class).saveUserContent("DSH", input);
	}

	@Deprecated
	public List<DshContent> getContent() {
		MenuTreeNode menu = null;
		for (MenuTreeNode item : BeanUtils.get(MenuService.class).getListPermitted().getContent()) {
			if (!"CM9020".equals(item.getId())) {
				continue;
			}
			menu = item;
		}
		if (menu == null || ObjectUtils.isEmpty(menu.getChildren())) {
			return Collections.emptyList();
		}
		List<DshContent> list = new ArrayList<>();
		menu.getChildren().forEach(item -> list.add(new DshContent(StringUtils.replace(item.getId(), "DASHBOARD-", ""))));
		return list;
	}

	@Deprecated
	public List<DshPosition> getPosition() {
		List<DshPosition> list = OptionUtils.getUserContent("DASHBOARD_POSITION", new TypeReference<List<DshPosition>>() {
		});

		// TODO Remove This block after UI improve FE displaying empty Position logic
		if (ObjectUtils.isEmpty(list)) {
			DshPosition quickStat = new DshPosition();
			quickStat.setType("QUICK_STATS");
			quickStat.setEnabled(true);
			list.add(quickStat);
			DshPosition distribution = new DshPosition();
			distribution.setType("DISTRIBUTION");
			distribution.setEnabled(true);
			list.add(distribution);
		}

		return list;
	}

	@Deprecated
	public void savePosition(List<DshPosition> list) {
		BeanUtils.get(OptionService.class).saveUserContent("DASHBOARD_POSITION", list);
	}

}
