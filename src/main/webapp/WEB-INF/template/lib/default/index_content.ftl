<#--自定义宏 Start-->

<#--数据日期列表-->
<#macro 数据日期列表 data 行数=5 长度=36 charset="utf-8" lang="zh-CN"> 
	<#if data?exists && (data.list?size gt 0)>
		<#list data.list as item>
			<li>
				<a href="${item.href}" target="_blank">${substrbyte(item.title, 长度, '...')}</a>
				<span>${item.pubTime}</span>
			</li>
		</#list>
	<#else>
		<br>未配置
	</#if>
</#macro>

<#-- 焦点轮播图 -->
<#macro 焦点轮播图 data 行数=5 宽度=502 高度=266 charset="utf-8" lang="zh-CN"> 
	<#if data?exists && (data.list?size gt 0)>
		<#list data.list as item>
			<li>
				<a target="_blank" href="${item.href}">
					<img src="${item.img}" width="${宽度}" height="${高度}" />
				</a>
			</li>
		</#list>
	<#else>
		<br>未配置
	</#if>
</#macro>

<#--普通列表-->
<#macro 列表 data 行数=5 长度=36 charset="utf-8" lang="zh-CN"> 
	<#if data?exists && (data.list?size gt 0)>
		<#list data.list as item>
			<li>·<a href="${item.href}" target="_blank">${substrbyte(item.title, 长度, '...')}</a></li>
		</#list>
	<#else>
		<br>未配置
	</#if>
</#macro>

<#--数据编号列表-->
<#macro 数据编号列表 data 行数=5 长度=36 charset="utf-8" lang="zh-CN"> 
	<#if data?exists && (data.list?size gt 0)>
		<#list data.list as item>
			<li>[${item_index+1}]<a href="${item.href}" target="_blank">${substrbyte(item.title, 长度, '...')}</a></li>
		</#list>
	<#else>
		<br>未配置
	</#if>
</#macro>

<#--单图新闻-->
<#macro 单图新闻 data 行数=5 长度=36 宽度=502 高度=266 charset="utf-8" lang="zh-CN"> 
	<#if data?exists && (data.list?size gt 0)>
		<#list data.list as item>
			<div><img src="${item.img}" width="${宽度}" height="${高度}" /></div>
		    <div class="titles"> <a href="${item.href}">${substrbyte(item.title, 长度, '...')}</a></div>
		</#list>
	<#else>
		<br>未配置
	</#if>
</#macro>

<#--主图列表-->
<#macro 主图列表 data 行数=5 长度=36 charset="utf-8" lang="zh-CN"> 
	<#if data?exists && (data.list?size gt 0)>
		<#list data.list as item>
			<#if item_index == 0>
				<div class="newtop">
	              <div class="toppic"><a href="${item.href}"><img src="${item.img}" width="108" height="70" /></a></div>
	              <div class="title7">
	                <h2><a href="${item.href}">${substrbyte(item.title, 长度, '...')}</a></h2>
	              </div>
	            </div>
	            <ul class="list10">
			<#else>
				<li>·<a href="${item.href}">${substrbyte(item.title, 长度, '...')}</a></li>
		   		<#if !(item_has_next)>
					</ul>
				 </#if>
			</#if>
		</#list>
	<#else>
		<br>未配置
	</#if>
</#macro>

<#--自定义宏 End-->

<#--自定义函数-->
<#assign substrbyte= "cn.com.people.data.util.FtlSubStringMethod"?new()>