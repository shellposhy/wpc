package cn.com.cms.view.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import cn.com.cms.framework.base.BaseController;
import cn.com.cms.framework.base.ControllerOperator;
import cn.com.cms.framework.base.Result;
import cn.com.cms.framework.base.table.DataTablesVo;
import cn.com.cms.framework.base.tree.DefaultTreeNode;
import cn.com.cms.framework.base.view.BaseMappingJsonView;
import cn.com.cms.framework.config.AppConfig;
import cn.com.cms.framework.config.JsonPara;
import cn.com.cms.view.constant.EModelType;
import cn.com.cms.view.model.ViewItem;
import cn.com.cms.view.model.ViewModel;
import cn.com.cms.view.model.ViewModelCategory;
import cn.com.cms.view.model.ViewPage;
import cn.com.cms.view.service.ViewItemService;
import cn.com.cms.view.service.ViewModelService;
import cn.com.cms.view.service.ViewPageService;
import cn.com.cms.view.vo.FileNodeVo;
import cn.com.cms.view.vo.ViewModelVo;

/**
 * 页面模板控制类
 * 
 * @author shishb
 * @version 1.0
 */
@Controller
@RequestMapping("/admin/view/model")
public class ViewModelController extends BaseController {
	private static final Logger LOG = LoggerFactory.getLogger(ViewModelController.class);

	@Resource
	private AppConfig appConfig;
	@Resource
	private ViewModelService viewModelService;
	@Resource
	private ViewPageService viewPageService;
	@Resource
	private ViewItemService viewItemService;

	/**
	 * 默认列表页
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String list() {
		LOG.debug("====view.model.list====");
		return "/admin/view/model/list";
	}

	/**
	 * 模板查询
	 * 
	 * @return
	 */
	@RequestMapping(value = "/s", method = RequestMethod.POST)
	public MappingJacksonJsonView search(@RequestBody JsonPara[] jsonParas) {
		LOG.debug("==view.model.search==");
		Map<String, String> jsonMap = JsonPara.getParaMap(jsonParas);
		Integer sEcho = Integer.parseInt(jsonMap.get(JsonPara.DataTablesParaNames.sEcho));
		Integer iDisplayStart = Integer.parseInt(jsonMap.get(JsonPara.DataTablesParaNames.iDisplayStart));
		int pageSize = appConfig.getAdminDataTablePageSize();
		int firstResult = 0;
		if (null != iDisplayStart) {
			firstResult = iDisplayStart;
		}
		Result<ViewModel> result = viewModelService.findByPage(firstResult, pageSize);
		List<ViewModelVo> list = new ArrayList<ViewModelVo>();
		int totalCount = 0;
		if (null != result && null != result.getList() && result.getList().size() > 0) {
			List<ViewModel> viewModels = result.getList();
			for (ViewModel viewModel : viewModels) {
				ViewModelVo viewModelVo = new ViewModelVo();
				viewModelVo = ViewModelVo.convertFromViewModel(viewModel);
				list.add(viewModelVo);
			}
			totalCount = result.getTotalCount();
		}
		DataTablesVo<ViewModelVo> dataTablesVo = new DataTablesVo<ViewModelVo>(sEcho, totalCount, totalCount, list);
		MappingJacksonJsonView mv = new BaseMappingJsonView();
		mv.addStaticAttribute("dataTablesVo", dataTablesVo);
		return mv;
	}

	/**
	 * 获得模板树
	 * 
	 * @return
	 */
	@RequestMapping(value = "/directory/tree", method = RequestMethod.POST)
	public MappingJacksonJsonView getDirectoryTree() {
		LOG.debug("==view.model.category.tree==");
		List<ViewModelCategory> cats = viewModelService.findAllCategory();
		DefaultTreeNode tree = null;
		if (cats.size() > 0) {
			tree = DefaultTreeNode.parseTree(cats);
			tree.name = "根分类";
		}
		MappingJacksonJsonView mv = new BaseMappingJsonView();
		mv.addStaticAttribute("directoryTree", tree);
		return mv;
	}

	/**
	 * 根据分类ID查找模板列表
	 * 
	 * @param categoryId
	 * @return
	 */
	@RequestMapping("/find/{categoryId}")
	public MappingJacksonJsonView find(@PathVariable("categoryId") Integer categoryId) {
		LOG.debug("==view.model.find==");
		List<ViewModel> models = viewModelService.findModelByCategoryId(categoryId);
		List<ViewModelVo> viewModelVos = new ArrayList<ViewModelVo>();
		for (ViewModel viewModel : models) {
			ViewModelVo viewModelVo = ViewModelVo.convertFromViewModel(viewModel);
			String picPath = appConfig.getTemplatePath() + "/" + viewModel.getCode() + "/logo.jpg";
			File pic = new File(picPath);
			if (pic.exists()) {
				viewModelVo.setImgUrl(appConfig.getTemplateHome() + "/" + viewModel.getCode() + "/logo.jpg");
			}
			viewModelVos.add(viewModelVo);
		}
		MappingJacksonJsonView mv = new BaseMappingJsonView();
		mv.addStaticAttribute("models", viewModelVos);
		return mv;
	}

	/**
	 * 新建分类
	 * 
	 * @param parentId
	 * @param model
	 * @return
	 */
	@RequestMapping("/directory/new/{parentId}")
	public String preNewDirectory(@PathVariable("parentId") int parentId, Model model) {
		LOG.debug("==view.model.category.new==");
		ViewModelCategory category = new ViewModelCategory();
		category.setParentID(parentId);
		model.addAttribute("category", category);
		return "/admin/view/model/directory/edit";
	}

	/**
	 * 编辑分类
	 * 
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping("/directory/{id}/edit")
	public String editDirectory(@PathVariable("id") int id, Model model) {
		LOG.debug("==view.model.category.edit==");
		ViewModelCategory category = viewModelService.findCategoryById(id);
		model.addAttribute("category", category);
		return "/admin/view/model/directory/edit";
	}

	/**
	 * 删除分类
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/directory/{id}/delete", method = RequestMethod.POST)
	public MappingJacksonJsonView deleteDirectory(@PathVariable("id") Integer id) {
		LOG.debug("==view.model.category.delete==");
		MappingJacksonJsonView mv = new BaseMappingJsonView();
		List<ViewModel> models = viewModelService.findModelByCategoryId(id);
		List<ViewModelCategory> categories = viewModelService.findCategoryByParentId(id);
		if (models.size() > 0 || categories.size() > 0) {
			mv.addStaticAttribute("error", true);
			mv.addStaticAttribute("msg", "该分类下有子分类或者模板，无法删除！");
		} else {
			viewModelService.deleteCategory(id);
			mv.addStaticAttribute("error", false);
		}
		return mv;
	}

	/**
	 * 保存分类
	 * 
	 * @param category
	 * @param result
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/directory/save", method = RequestMethod.POST)
	public String saveDirectory(@Valid ViewModelCategory category, BindingResult result, final Model model) {
		LOG.debug("==view.model.category.save==");
		category.setOrderId(0);
		viewModelService.saveCategory(category);
		return "/admin/view/model/list";
	}

	/**
	 * 进入模板文件管理页面
	 * 
	 * @param viewModeId
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/{id}/readFiles", method = RequestMethod.GET)
	public String readFiles(@PathVariable("id") Integer viewModeId, Model model, HttpServletRequest request,
			HttpServletResponse response) {
		LOG.debug("===view.model.readFiles===");
		ViewModel viewModel = viewModelService.find(viewModeId);
		model.addAttribute("viewModel", viewModel);
		return "/admin/view/model/fileMng";
	}

	/**
	 * 导入模板文件
	 * 
	 * @param file
	 * @param viewModelId
	 * @return
	 */
	@RequestMapping(value = "/import/{id}", method = RequestMethod.POST)
	public String importModel(@RequestParam("file") MultipartFile file, @PathVariable("id") Integer viewModelId,
			Model model) {
		LOG.debug("===view.model.import===");
		ViewModel viewModel = viewModelService.find(viewModelId);
		viewModelService.uploadModel(file, viewModel.getCode());
		model.addAttribute("viewModel", viewModel);
		return "/admin/view/model/edit";
	}

	/**
	 * 通过文件的绝对路径读取文件
	 * 
	 * @param jsonPara
	 * @param model
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/path/readFile", method = RequestMethod.POST)
	public MappingJacksonJsonView readFileByPath(@RequestBody JsonPara jsonPara, HttpServletRequest request,
			HttpServletResponse response) {
		LOG.debug("===view.model.path.readFiles===");
		String filePath = jsonPara.value;
		String fileContent = "";
		try {
			fileContent = readFile(filePath.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/html;charset=UTF-8");
		MappingJacksonJsonView mv = new BaseMappingJsonView();
		mv.addStaticAttribute("fileContent", fileContent);
		return mv;
	}

	/**
	 * 保存修改后文件内容
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/saveFile", method = RequestMethod.POST)
	public MappingJacksonJsonView saveFileByPath(HttpServletRequest request) {
		LOG.debug("===view.model.path.saveFiles===");
		String filePath = request.getParameter("filePath");
		String fileContent = request.getParameter("fileContent");
		if (fileContent == null) {
			fileContent = "";
		}
		try {
			writeFile(fileContent, filePath.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		MappingJacksonJsonView mv = new BaseMappingJsonView();
		mv.addStaticAttribute("result", "success");
		return mv;
	}

	/**
	 * 修改模板
	 * 
	 * @param viewModelId
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
	public String edit(@PathVariable(value = "id") Integer viewModelId, Model model)
			throws JsonGenerationException, JsonMappingException, IOException {
		LOG.debug("===view.model.edit===");
		ViewModel viewModel = viewModelService.find(viewModelId);
		model.addAttribute("viewModel", viewModel);
		return "/admin/view/model/edit";
	}

	/**
	 * 保存模板
	 * 
	 * @param viewModel
	 * @param file
	 * @param result
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String save(@Valid final ViewModel viewModel, @RequestParam("file") MultipartFile file, BindingResult result,
			final Model model, HttpServletRequest request) {
		LOG.debug("===view.model.save===");
		String ret = super.save(viewModel, result, model, new ControllerOperator() {
			public void operate() {
				viewModel.setFileName("index.html");
				viewModelService.save(viewModel);
			}

			public void onFailure() {

			}

			public String getSuccessView() {
				return "redirect:/admin/view/model";
			}

			public String getFailureView() {
				return "/admin/view/model/edit";
			}
		});
		viewModelService.uploadModel(file, viewModel.getCode());
		return ret;
	}

	/**
	 * 新增模板
	 * 
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/new/{categoryId}", method = RequestMethod.GET)
	public String preNew(Model model, @PathVariable("categoryId") Integer categoryId)
			throws JsonGenerationException, JsonMappingException, IOException {
		LOG.debug("===view.model.new===");
		ViewModel viewModel = new ViewModel();
		viewModel.setCategoryId(categoryId);
		viewModel.setOrderId(1);
		model.addAttribute(viewModel);
		return "/admin/view/model/edit";
	}

	/**
	 * 删除模板
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
	public String delete(@PathVariable("id") Integer id)
			throws JsonGenerationException, JsonMappingException, IOException {
		LOG.debug("===view.model.delete===");
		try {
			viewModelService.delete(new Integer[] { id });
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin/view/model";
	}

	/**
	 * 扫描模板
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/scan/{id}", method = RequestMethod.POST)
	@ResponseBody
	public void scan(@PathVariable("id") Integer id) {
		LOG.debug("===view.model.scan===");
		viewModelService.updateModelContent(id);
	}

	/**
	 * 获得文件结构树
	 * 
	 * @param viewModelId
	 * @return
	 */
	@RequestMapping(value = "/{id}/fileTree", method = RequestMethod.GET)
	public MappingJacksonJsonView getFileTree(@PathVariable("id") Integer viewModelId) {
		LOG.debug("===view.model.tree===");
		ViewModel model = viewModelService.find(viewModelId);
		StringBuilder rootPath = new StringBuilder();
		rootPath.append(appConfig.getTemplatePath()).append("/").append(model.getCode());
		File root = new File(rootPath.toString());
		FileNodeVo rootFileNode = null;
		if (root.exists()) {
			rootFileNode = new FileNodeVo();
			rootFileNode.setName(root.getName());
			rootFileNode.setDirectory(root.isDirectory());
			rootFileNode.setAbsolutePath(root.getAbsolutePath());
			String[] urls = root.getAbsolutePath().split("template");
			if (null != urls && urls.length > 1) {
				rootFileNode.setFileUrl("#");
			}
			if (root.isDirectory()) {
				getFileNodeTree(root, rootFileNode, model.getModelType());
			}
		}
		MappingJacksonJsonView mv = new BaseMappingJsonView();
		mv.addStaticAttribute("root", rootFileNode);
		return mv;
	}

	/**
	 * 获得文件内的HTML内容
	 * 
	 * @param pageId
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/contentHtml/{pageId}", produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String findContentHtml(@PathVariable int pageId, HttpServletRequest request) {
		LOG.debug("===view.model.file.content===");
		String itemCode = request.getParameter("itemCode");
		ViewPage viewPage = viewPageService.findById(pageId);
		ViewItem viewItem = viewItemService.findByModelIdAndCode(viewPage.getModelId(), itemCode);
		return viewItem.getContent();
	}

	/**
	 * 读取指定路径文件
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String path) throws IOException {
		File f = new File(path);
		StringBuffer res = new StringBuffer();
		FileInputStream fis = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		while ((line = br.readLine()) != null) {
			res.append(line).append("\r\n");
		}
		br.close();
		return res.toString();
	}

	/**
	 * 
	 * 读取文件存到指定路径
	 * 
	 * @param cont
	 * @param path
	 * @return
	 */
	public static boolean writeFile(String cont, String path) {
		try {
			File dist = new File(path);
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(dist), "UTF-8");
			writer.write(cont);
			writer.flush();
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获得文件结构树
	 * 
	 * @param root
	 * @param rootFileNode
	 * @param modelType
	 * @return
	 */
	private void getFileNodeTree(File root, FileNodeVo rootFileNode, EModelType modelType) {
		File[] files = root.listFiles();
		for (File file : files) {
			if (rootFileNode.getChildren() == null) {
				rootFileNode.setChildren(new ArrayList<FileNodeVo>());
			}
			FileNodeVo node = new FileNodeVo();
			node.setName(file.getName());
			node.setDirectory(file.isDirectory());
			node.setAbsolutePath(file.getAbsolutePath());
			String[] urls = file.getAbsolutePath().split("template");
			if (null != urls && urls.length > 1) {
				switch (modelType) {
				case Subject:
					node.setFileUrl(appConfig.getAppPath() + "/" + appConfig.getTemplateHome() + urls[1]);
					break;
				default:
					node.setFileUrl(appConfig.getAppPath() + "/" + appConfig.getTemplateHome() + urls[1]);
					break;
				}
			}
			rootFileNode.getChildren().add(node);
			if (file.isDirectory()) {
				getFileNodeTree(file, node, modelType);
			}
		}
	}
}
