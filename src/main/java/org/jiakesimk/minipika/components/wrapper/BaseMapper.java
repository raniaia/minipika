package org.jiakesimk.minipika.components.wrapper;

/* ************************************************************************
 *
 * Copyright (C) 2020 tiansheng All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ************************************************************************/

/*
 * Creates on 2019/11/13.
 */

import groovy.console.ui.ObjectBrowser;

import java.util.List;

/**
 * @author lts
 */
public interface BaseMapper<Entity>
{

  /**
   * 根据ID查询一条数据
   * @param id id
   */
  Entity select(String id);

  /**
   * 根据ID删除一条数据
   * @param id id
   * @return 影响行数
   */
  int delete(String id);

}