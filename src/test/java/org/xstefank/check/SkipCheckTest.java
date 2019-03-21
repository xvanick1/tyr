package org.xstefank.check;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.xstefank.TestUtils;
import org.xstefank.api.GitHubAPI;
import org.xstefank.model.yaml.FormatConfig;
import org.xstefank.model.yaml.SkipPatterns;
import java.util.regex.Pattern;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.powermock.api.support.membermodification.MemberMatcher.method;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GitHubAPI.class)
public class SkipCheckTest {

    private static FormatConfig formatConfig;
    private SkipPatterns skipPatterns;

    @Before
    public void setUp() {
        skipPatterns = new SkipPatterns();
        PowerMockito.suppress(method(GitHubAPI.class, TestUtils.READ_TOKEN));
        PowerMockito.stub(method(GitHubAPI.class, TestUtils.GET_JSON_WITH_COMMITS, JsonNode.class)).toReturn(TestUtils.TEST_COMMITS_PAYLOAD);
    }

    @Test (expected=IllegalArgumentException.class)
    public void testNullConfigParameter() throws IllegalArgumentException{
        SkipCheck.shouldSkip(TestUtils.TEST_PAYLOAD, null);
    }

    @Test (expected=IllegalArgumentException.class)
    public void testNullPayloadParameter() throws IllegalArgumentException{
        SkipCheck.shouldSkip(null, formatConfig);
    }

    @Test
    public void testSkipByTitleRegexMatch(){
        skipPatterns.setTitle(Pattern.compile("Test PR"));
        formatConfig = TestUtils.setUpFormatConfig(skipPatterns);

        Assert.assertTrue("Method cannot match valid title regex", SkipCheck.shouldSkip(TestUtils.TEST_PAYLOAD, formatConfig));
    }

    @Test
    public void testSkipByTitleRegexNonMatch(){
        skipPatterns.setTitle(Pattern.compile("can't.*match.*this"));
        formatConfig = TestUtils.setUpFormatConfig(skipPatterns);

        Assert.assertFalse("Method matched invalid title regex",SkipCheck.shouldSkip(TestUtils.TEST_PAYLOAD, formatConfig));
    }

    @Test
    public void testSkipByCommitRegexMatch(){
        skipPatterns.setCommit(Pattern.compile("Test commit"));
        formatConfig = TestUtils.setUpFormatConfig(skipPatterns);

        Assert.assertTrue("Method cannot match valid commit regex",SkipCheck.shouldSkip(TestUtils.TEST_PAYLOAD, formatConfig));
    }

    @Test
    public void testSkipByCommitRegexNonMatch(){
        skipPatterns.setCommit(Pattern.compile("can't.*match.*this"));
        formatConfig = TestUtils.setUpFormatConfig(skipPatterns);

        Assert.assertFalse("Method matched invalid commit regex",SkipCheck.shouldSkip(TestUtils.TEST_PAYLOAD, formatConfig));
    }

    @Test
    public void testSkipByPullRequestDescriptionRegexMatch(){
        skipPatterns.setDescription(Pattern.compile("Test description"));
        formatConfig = TestUtils.setUpFormatConfig(skipPatterns);

        Assert.assertTrue("Method cannot match valid description regex",SkipCheck.shouldSkip(TestUtils.TEST_PAYLOAD, formatConfig));
    }

    @Test
    public void testSkipByPullRequestDescriptionRegexNonMatch(){
        skipPatterns.setDescription(Pattern.compile("can't.*match.*this"));
        formatConfig = TestUtils.setUpFormatConfig(skipPatterns);

        Assert.assertFalse("Method matched invalid description regex",SkipCheck.shouldSkip(TestUtils.TEST_PAYLOAD, formatConfig));
    }

    @Test
    public void testShouldSkipEmptySkipPatterns() {
        formatConfig = TestUtils.setUpFormatConfig(skipPatterns);

        Assert.assertFalse("Invalid result after empty skipping patterns",SkipCheck.shouldSkip(TestUtils.TEST_PAYLOAD, formatConfig));
    }
}
