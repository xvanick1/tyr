/*
 * Copyright 2021 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.tyr.check;

import org.apache.commons.codec.language.bm.Rule;
import org.jboss.tyr.Check;
import org.jboss.tyr.InvalidPayloadException;
import org.jboss.tyr.github.GitHubService;
import org.jboss.tyr.model.Utils;
import org.jboss.tyr.model.yaml.RegexDefinition;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.regex.Matcher;

public class CommitRangeCheck implements Check {

	static final String DEFAULT_MESSAGE = "Number of commits is not within the allowed range";

	@Inject
	GitHubService gitHubService;

	private String range;

	public void setRegex(RegexDefinition commit) {
		if (commit == null || commit.getRange() == null) {
			throw new IllegalArgumentException("Input argument cannot be null");
		}
		this.range = commit.getRange();
	}

	@Override
	public String check(JsonObject payload) throws InvalidPayloadException {
		JsonArray commitsJsonArray = gitHubService.getCommitsJSON(payload);

		Matcher matcher = range.matches(String.valueOf(commitsJsonArray.size()));
		if (!matcher.matches()) {
			return DEFAULT_MESSAGE;
		}

		return null;
	}

}
