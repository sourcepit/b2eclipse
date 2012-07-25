/**
 * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sourcepit.b2eclipse.input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Marco Grupe <marco.grupe@googlemail.com>
 */
public class TreeViewerInput {

	private File[] projects;
	private List<File> projectFileList = new ArrayList<File>();
	private List<String> dirList = new ArrayList<String>();
	private List<Category> categories;

	public TreeViewerInput() {
	};

	public TreeViewerInput(Object inputElement) {

		File[] elementList = ((File) inputElement).listFiles();

		if (elementList != null) {

			getProjects(inputElement);

		}

		projects = new File[projectFileList.size()];
		for (int y = 0; y < projects.length; y++) {
			projects[y] = projectFileList.get(y);

		}
	}

	public List<Category> getData() {
		categories = new ArrayList<Category>();

		Category categoryModules = new Category();
		categoryModules.setName("Plugins");
		categories.add(categoryModules);

		Category categoryTests = new Category();
		categoryTests.setName("Tests");
		categories.add(categoryTests);

		Category categoryDocs = new Category();
		categoryDocs.setName("Docs");
		categories.add(categoryDocs);

		for (int i = 0; i < projects.length; i++) {
			if (!projects[i].getParent().endsWith(".tests")
					&& !new File(projects[i].getParent()).getParent().endsWith(
							"tests")
					&& !projects[i].getParent().endsWith(".doc")
					&& !new File(projects[i].getParent()).getParent().endsWith(
							"doc")) {
				categoryModules.getModules().add(projects[i]);
			}
			if (projects[i].getParent().endsWith(".tests")
					|| new File(projects[i].getParent()).getParent().endsWith(
							"tests")) {
				categoryTests.getModules().add(projects[i]);
			}
			if (projects[i].getParent().endsWith(".doc")
					|| new File(projects[i].getParent()).getParent().endsWith(
							"doc")) {
				categoryDocs.getModules().add(projects[i]);
			}
		}

		return categories;
	}

	private List<File> getProjects(Object inputElement) {

		File[] elementList = ((File) inputElement).listFiles();
		getDirList().clear();

		for (File i : elementList) {
			setDirList(i.getName());
		}

		if ((dirList.contains("module.xml") && !(dirList.contains(".project")))
				|| (!(dirList.contains("module.xml")) && !(dirList
						.contains(".project")))) {
			doModuleSearch(elementList);
		}

		else if (!(dirList.contains("module.xml"))
				&& dirList.contains(".project")) {
			doProjectSearch(elementList);
		}

		return projectFileList;
	}

	public void clearArrayList() {
		projectFileList.clear();
	}

	public List<File> getProjectFileList() {
		return projectFileList;
	}

	public List<Category> getCategories() {
		return categories;
	}

	private List<String> getDirList() {
		return dirList;
	}

	private void setDirList(String file) {
		dirList.add(file);
	}

	private void doModuleSearch(File[] elementList) {

		for (File file : elementList) {
			if (file.isDirectory() && !(file.getName().startsWith("."))
					&& !(file.getName().equals("target"))) {
				getProjects(file);
			}
		}
	}

	private void doProjectSearch(File[] elementList) {
		for (File file : elementList) {
			if (file.getName().equals(".project")) {
				projectFileList.add(file.getAbsoluteFile());
			}
		}
	}

}
