package cn.com.cms.user.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import cn.com.cms.framework.base.Node;
import cn.com.cms.framework.base.Result;
import cn.com.cms.library.model.BaseLibrary;
import cn.com.cms.library.service.LibraryService;
import cn.com.cms.user.constant.EActionType;
import cn.com.cms.user.dao.UserActionMapMapper;
import cn.com.cms.user.dao.UserDataAuthorityMapper;
import cn.com.cms.user.dao.UserGroupMapMapper;
import cn.com.cms.user.dao.UserGroupMapper;
import cn.com.cms.user.model.UserActionMap;
import cn.com.cms.user.model.UserDataAuthority;
import cn.com.cms.user.model.UserGroup;

/**
 * 用户组权限服务类
 * 
 * @author shishb
 * @version 1.5
 */
@Service
public class UserGroupService {
	Logger log = Logger.getLogger(UserGroupService.class.getName());
	@Resource
	private UserGroupMapper userGroupMapper;
	@Resource
	private UserActionMapMapper userActionMapMapper;
	@Resource
	private UserGroupMapMapper userGroupMapMapper;
	@Resource
	private UserDataAuthorityMapper userDataAuthorityMapper;
	@Resource
	private LibraryService<?> libraryService;

	/**
	 * 分页查询用户组
	 * 
	 * @param qs
	 * @param first
	 * @param size
	 * @return Result
	 */
	public Result<UserGroup> search(String qs, int first, int size) {
		Result<UserGroup> result = new Result<UserGroup>();
		if (qs == null || qs.equals("")) {
			result.setList(userGroupMapper.search(first, size));
			result.setTotalCount(userGroupMapper.count());
		} else {
			result.setList(userGroupMapper.searchByQs(qs, first, size));
			result.setTotalCount(userGroupMapper.countByQs(qs));
		}
		return result;
	}

	/**
	 * 保存新增，更新用户组
	 * 
	 * @param userGroup
	 * @return void
	 */
	public void save(UserGroup userGroup) {
		Integer id = userGroup.getId();
		if (id == null) {
			userGroupMapper.insert(userGroup);
			id = userGroup.getId();
		} else {
			userDataAuthorityMapper.deleteByGroupID(id);
			userActionMapMapper.deleteByGroupId(id);
			userGroupMapper.update(userGroup);
		}
		// admin access
		List<Integer> actionList = userGroup.getActionList();
		if (!userGroup.getAllAdminAuthority() && actionList.size() != 0) {
			List<UserActionMap> userActionMapList = new ArrayList<UserActionMap>(actionList.size());
			for (Integer actionId : actionList) {
				UserActionMap userActionMap = new UserActionMap();
				userActionMap.setActionID(actionId);
				userActionMap.setGroupID(userGroup.getId());
				userActionMap.setType(0);
				userActionMapList.add(userActionMap);
			}
			userActionMapMapper.batchInsert(userActionMapList);
		}
		// database access
		if (!userGroup.getAllDataAuthority()) {
			Integer[] readableIds = userGroup.getReadableIds();
			Integer[] writableIds = userGroup.getWritableIds();
			Integer[] viewableIds = userGroup.getViewableIds();
			Integer[] downableIds = userGroup.getDownloadableIds();
			Integer[] printableIds = userGroup.getPrintableIds();
			if (null != readableIds && readableIds.length > 0) {
				List<UserDataAuthority> dataAuthorityList = Lists.newArrayList();
				for (Integer baseId : readableIds) {
					BaseLibrary<?> dataBase = libraryService.find(baseId);
					UserDataAuthority dataAuthority = new UserDataAuthority();
					if (null != dataBase) {
						dataAuthority.setGroupId(id);
						dataAuthority.setAllDataTime(true);
						dataAuthority.setCreateTime(userGroupMapper.find(id).getCreateTime());
						dataAuthority.setUpdateTime(new Date());
						dataAuthority.setCreatorId(userGroupMapper.find(id).getCreatorId());
						dataAuthority.setUpdaterId(userGroup.getUpdaterId());
						dataAuthority.setObjId(baseId);
						dataAuthority.setObjType(dataBase.getType());
						if (Arrays.asList(writableIds).contains(baseId)) {
							dataAuthority.addAllowActionType(EActionType.Write);
						}
						if (Arrays.asList(viewableIds).contains(baseId)) {
							dataAuthority.addAllowActionType(EActionType.View);
						}
						if (Arrays.asList(downableIds).contains(baseId)) {
							dataAuthority.addAllowActionType(EActionType.Download);
						}
						if (Arrays.asList(printableIds).contains(baseId)) {
							dataAuthority.addAllowActionType(EActionType.Print);
						}
						dataAuthorityList.add(dataAuthority);
					}
				}
				if (dataAuthorityList.size() > 0) {
					userDataAuthorityMapper.batchInsert(dataAuthorityList);
				}
			}
		}
	}

	/**
	 * 删除用户组，可做批量删除
	 * 
	 * @param groupIds
	 * @return void
	 */
	public void delete(Integer[] groupIds) {
		int length = groupIds.length;
		if (length == 1) {
			userActionMapMapper.deleteByGroupId(groupIds[0]);
			userGroupMapMapper.deleteByGroupId(groupIds[0]);
			userDataAuthorityMapper.deleteByGroupID(groupIds[0]);
			userGroupMapper.delete(groupIds[0]);
		} else if (length > 1) {
			for (int i = 0; i < length; i++) {
				userActionMapMapper.deleteByGroupId(groupIds[i]);
				userGroupMapMapper.deleteByGroupId(groupIds[i]);
				userDataAuthorityMapper.deleteByGroupID(groupIds[i]);
			}
			userGroupMapper.batchDelete(groupIds);
		}
	}

	/**
	 * 通过groupId获得组
	 * 
	 * 用于更新用户组信息
	 * 
	 * @param id
	 * @return UserGroup
	 */
	public UserGroup find(Integer id) {
		return userGroupMapper.find(id);
	}

	/**
	 * 根据用户组名称查询
	 * 
	 * @param name
	 * @return
	 */
	public UserGroup findByName(String name) {
		return userGroupMapper.findByName(name);
	}

	/**
	 * 查询是否没有用户的用户组
	 * 
	 * @param
	 */
	public List<UserGroup> findHasUser() {
		return userGroupMapper.findHasUser();
	}

	/**
	 * 根据用户编号查找用户组
	 * 
	 * @param userId
	 * @return
	 */
	public List<UserGroup> findByUserId(Integer userId) {
		return userGroupMapper.findByUserId(userId);
	}

	/**
	 * 查询全部
	 * 
	 * @return
	 */
	public List<UserGroup> findAll() {
		return userGroupMapper.findAll();
	}

	/**
	 * 获取节点列表
	 * 
	 * @param userId
	 *            用户id
	 * @return List
	 */
	public List<Node<Integer, String>> findList(Integer userId) {
		List<Node<Integer, String>> nodeList = new ArrayList<Node<Integer, String>>();
		List<UserGroup> userGroupList = userGroupMapper.findAll();
		if (userId != null) {
			List<Integer> groupIds = userGroupMapMapper.findGroupIdsByUserId(userId);
			for (UserGroup userGroup : userGroupList) {
				if (groupIds.contains(userGroup.getId())) {
					Node<Integer, String> node = new Node<Integer, String>();
					node.id = userGroup.getId();
					node.name = userGroup.getName();
					nodeList.add(node);
				}
			}
		} else {
			for (UserGroup userGroup : userGroupList) {
				Node<Integer, String> node = new Node<Integer, String>();
				node.id = userGroup.getId();
				node.name = userGroup.getName();
				nodeList.add(node);
			}
		}
		return nodeList;
	}

	/**
	 * 判读是否具有全部数据权限
	 * 
	 * @param userId
	 * @return
	 */
	public boolean isAllDataAuthority(Integer userId) {
		List<UserGroup> userGroupList = findByUserId(userId);
		for (UserGroup userGroup : userGroupList) {
			if (userGroup.getAllDataAuthority()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 统计所有用户组
	 */
	public Integer count() {
		return userGroupMapper.count();
	}

	/**
	 * 统计今日新增所有用户组
	 */
	public Integer countByToday() {
		return userGroupMapper.countByToday();
	}

}
