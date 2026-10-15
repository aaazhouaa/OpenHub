

package com.thirtydegreesray.openhub.ui.widget.webview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thirtydegreesray.openhub.mvp.model.GitHubName;
import com.thirtydegreesray.openhub.util.AppUtils;
import com.thirtydegreesray.openhub.util.GitHubHelper;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2017/8/20 12:58:43
 * Copied from Copyright (C) 2017 Kosh.
 * Modified by Copyright (C) 2017 ThirtyDegreesRay.
 */

class HtmlHelper {
    private static final Pattern LINK_PATTERN = Pattern.compile("href=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMAGE_PATTERN = Pattern.compile("src=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);

    static String generateImageHtml(@NonNull String imageUrl, @NonNull String backgroundColor) {
        boolean isDark = AppUtils.isNightMode();
        String imgFilter = isDark ? "opacity: 0.70; filter: brightness(0.70);" : "";
        return "<html>" +
                "<head>" +
                "<meta name=\"referrer\" content=\"no-referrer\" />" +
                "<style>" +
                "img{height: auto; width: 100%; " + imgFilter + "}" +
                "body{background: " + backgroundColor + ";}" +
                "</style>" +
                "</head>" +
                "<body><img src=\"" + imageUrl + "\"/></body>" +
                "</html>";
    }

    static String generateHtmlSourceHtml(@NonNull String htmlSource, @NonNull String backgroundColor,
                                         @NonNull String accentColor) {
        boolean isDark = AppUtils.isNightMode();
        String imgFilter = isDark ? "img{opacity: 0.70; filter: brightness(0.70);}\n" : "";
        return "<html>" +
                    "<head>" +
                        "<meta charset=\"utf-8\" />\n" +
                        "<meta name=\"referrer\" content=\"no-referrer\" />\n" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
                        "<style>" +
                            "body{background: " + backgroundColor + ";}" +
                            "a {color:" + accentColor + " !important;}" +
                            imgFilter +
                        "</style>" +
                    "</head>" +
                    "<body>" +
                        htmlSource +
                    "</body>" +
                "</html>";
    }

    static String generateCodeHtml(@NonNull String codeSource, @Nullable String extension,
                                   boolean isDark, @NonNull String backgroundColor,
                                   boolean wrap, boolean lineNums) {
        String skin = isDark ? "github-dark.min" : "github.min";
        return generateCodeHtml(codeSource, extension, skin, backgroundColor, wrap, lineNums);
    }

    private static String guessLanguage(@Nullable String extension) {
        if (extension == null) return null;
        final Map<String, String> langMap = Map.of("h", "cpp",
                                                   "hpp", "cpp",
                                                   "cpp", "cpp",
                                                   "cc", "cpp",
                                                   "cxx", "cpp",
                                                   "c", "c",
                                                   "kt", "kotlin",
                                                   "java", "java",
                                                   "rs", "rust");

        if (langMap.containsKey(extension)) {
            return langMap.get(extension);
        }
        else {
            return null;
        }
    }

    private static String generateCodeHtml(@NonNull String codeSource, @Nullable String extension,
                                           @Nullable String skin, @NonNull String backgroundColor,
                                           boolean wrap, boolean lineNums) {
        String lang = guessLanguage(extension);
        return "<html>\n" +
                    "<head>\n" +
                        "<meta charset=\"utf-8\" />\n" +
                        "<title>Code View</title>\n" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
                        "<link rel=\"stylesheet\" href=\"./highlight/styles/" + skin + ".css\">\n" +
                        "<script src=\"./highlight/highlight.min.js\"></script>\n" +
                        "<script>\n" +
                        "document.addEventListener('DOMContentLoaded', (event) => {\n" +
                        "   document.querySelectorAll('pre code').forEach((el) => {\n" +
                        "       hljs.highlightAll(el);\n" +
                                (lineNums ? "let lines = el.innerHTML.split('\\n');\n" +
                                            "lines.shift();\n" +
                                            "var lineNumberWidth = lines.length.toString().length * 10" + (wrap ? " + 4" : "") + ";\n" +
                                            "var css = document.createElement('style');\n" +
                                            "css.type = 'text/css';\n" +
                                            "var newStyle = `.line-number {\\n" +
                                            "  display: inline-block;\\n" +
                                            "  width: ${lineNumberWidth}px;\\n" +
                                            "  text-align: right;\\n" +
                                            "  color: #999;\\n" +
                                            "  margin-right: 16px;\\n" +
                                            "  user-select: none;\\n" +
                                            "}`;\n" +
                                            "css.innerHTML = newStyle;\n" +
                                            "document.head.appendChild(css);\n" +
                                            "el.innerHTML = lines.map(function(line, index) {\n" +
                                            "   return '<span class=\"line-number\">' + (index + 1) + '.' + '</span>' + line;\n" +
                                            "}).join('\\n');" : "") +
                        "   })\n" +
                        "})\n" +
                        "</script>\n" +
                        "<style>" +
                            "body {background: " + backgroundColor + ";}\n" +
                            "code {\n" +
                            "    word-wrap: " + (wrap ? "break-word" : "normal") + ";\n" +
                            "    white-space: " + (wrap ? "pre-wrap" : "no-wrap") + ";\n" +
                            "}\n" +
                        "</style>" +
                    "</head>\n" +
                    "<body>\n" +
                        "<pre><code" + (lang != null ? " class=\"language-" + lang + "\"" : "") +">\n" +
                            formatCode(codeSource) +
                        "</pre>\n" +
                    "</body>\n" +
                "</html>";
    }

    static String generateMdHtml(@NonNull String mdSource, @Nullable String baseUrl,
                                 boolean isDark, @NonNull String backgroundColor,
                                 @NonNull String accentColor, boolean wrapCode) {
        String skin = isDark ? "markdown_dark.css" : "markdown_white.css";
        mdSource = StringUtils.isBlank(baseUrl) ? mdSource : fixLinks(mdSource, baseUrl);
        //fix wiki inner url like this "href="/robbyrussell/oh-my-zsh/wiki/Themes""
        mdSource = fixWikiLinks(mdSource);
        return generateMdHtml(mdSource, skin, backgroundColor, accentColor, wrapCode);
    }

    private static String generateMdHtml(@NonNull String mdSource, String skin,
                                         @NonNull String backgroundColor,
                                         @NonNull String accentColor, boolean wrapCode) {
        return "<html>\n" +
                    "<head>\n" +
                        "<meta charset=\"utf-8\" />\n" +
                        "<meta name=\"referrer\" content=\"no-referrer\" />\n" +
                        "<title>MD View</title>\n" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0\"/>" +
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\"./" + skin + "\">\n" +
                        "<style>" +
                            "body{background: " + backgroundColor + ";}\n" +
                            "a {color:" + accentColor + " !important;}\n" +
                            "table {display: block; overflow-x: auto; max-width: 100%;}\n" +
                            "img {max-width: 100%;}\n" +
                            ".highlight pre, pre {\n" +
                            " word-wrap: " + (wrapCode ? "break-word" : "normal") + "; \n" +
                            " white-space: " + (wrapCode ? "pre-wrap" : "pre") + "; \n" +
                            "}\n" +
                        "</style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                        mdSource + "\n" +
                    "</body>\n" +
                "</html>";
    }

    private static String formatCode(@NonNull String codeSource) {
        if (StringUtils.isBlank(codeSource)) return codeSource;
        return codeSource.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

    private static String fixWikiLinks(@NonNull String source) {
        Matcher linksMatcher = LINK_PATTERN.matcher(source);
        while (linksMatcher.find()) {
            String oriUrl = linksMatcher.group(1);
            if (oriUrl != null && oriUrl.startsWith("/") && oriUrl.contains("/wiki/")) {
                String fixedUrl = "https://github.com" + oriUrl;
                source = source.replace(linksMatcher.group(0), "href=\"" + fixedUrl + "\"");
            }
        }
        return source;
    }

    private static String fixLinks(@NonNull String source, @NonNull String baseUrl) {
        GitHubName gitHubName = GitHubName.fromUrl(baseUrl);
        if (gitHubName == null) return source;
        String owner = gitHubName.getUserName();
        String repo = gitHubName.getRepoName();
        if (StringUtils.isBlank(owner) || StringUtils.isBlank(repo)) return source;

        String branch = "master";
        if (baseUrl.contains("blob/")) {
            int blobIndex = baseUrl.indexOf("blob/") + 5;
            int nextSlash = baseUrl.indexOf("/", blobIndex);
            if (nextSlash != -1) {
                branch = baseUrl.substring(blobIndex, nextSlash);
            } else {
                branch = baseUrl.substring(blobIndex);
            }
        }

        Matcher linksMatcher = LINK_PATTERN.matcher(source);
        while (linksMatcher.find()) {
            String oriUrl = linksMatcher.group(1);
            if (StringUtils.isBlank(oriUrl)) continue;
            if (oriUrl.contains("http://") || oriUrl.contains("https://")
                    || oriUrl.startsWith("#") || oriUrl.startsWith("mailto:")) {
                continue;
            }

            String cleanUrl = cleanRelativePrefix(oriUrl);
            String subUrl = cleanUrl.startsWith("/") ? cleanUrl : "/" + cleanUrl;
            String fixedUrl;
            if (!GitHubHelper.isImage(cleanUrl)) {
                fixedUrl = "https://github.com/" + owner + "/" + repo + "/blob/" + branch + subUrl;
            } else {
                fixedUrl = "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + subUrl;
            }
            source = source.replace(linksMatcher.group(0), "href=\"" + fixedUrl + "\"");
        }

        Matcher imagesMatcher = IMAGE_PATTERN.matcher(source);
        while (imagesMatcher.find()) {
            String oriUrl = imagesMatcher.group(1);
            if (StringUtils.isBlank(oriUrl)) continue;

            // GitHub blob 地址返回的是 HTML 页面，作为 img src 必定无法渲染，转换为 raw 资源链接
            if (oriUrl.contains("github.com") && oriUrl.contains("/blob/")) {
                String fixedUrl = oriUrl.replace("github.com", "raw.githubusercontent.com")
                        .replace("/blob/", "/");
                source = source.replace(imagesMatcher.group(0), "src=\"" + fixedUrl + "\"");
                continue;
            }

            // 已经是完整网络链接
            if (oriUrl.startsWith("http://") || oriUrl.startsWith("https://")) {
                continue;
            }
            if (oriUrl.startsWith("//")) {
                source = source.replace(imagesMatcher.group(0), "src=\"https:" + oriUrl + "\"");
                continue;
            }

            String cleanUrl = cleanRelativePrefix(oriUrl);
            String fixedUrl;
            // 避免对已包含仓库前缀的相对路径重复拼接
            if (cleanUrl.startsWith("/" + owner + "/" + repo + "/raw/") ||
                    cleanUrl.startsWith(owner + "/" + repo + "/raw/")) {
                String sub = cleanUrl.startsWith("/") ? cleanUrl : "/" + cleanUrl;
                fixedUrl = "https://raw.githubusercontent.com" + sub;
            } else if (cleanUrl.startsWith("/" + owner + "/" + repo + "/blob/") ||
                    cleanUrl.startsWith(owner + "/" + repo + "/blob/")) {
                String sub = cleanUrl.startsWith("/") ? cleanUrl : "/" + cleanUrl;
                fixedUrl = "https://raw.githubusercontent.com" + sub.replace("/blob/", "/");
            } else {
                String subUrl = cleanUrl.startsWith("/") ? cleanUrl : "/" + cleanUrl;
                fixedUrl = "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + subUrl;
            }
            source = source.replace(imagesMatcher.group(0), "src=\"" + fixedUrl + "\"");
        }

        return source;
    }

    private static String cleanRelativePrefix(@NonNull String url) {
        String res = url.trim();
        while (res.startsWith("./")) {
            res = res.substring(2);
        }
        while (res.startsWith("../")) {
            res = res.substring(3);
        }
        return res;
    }

    static String generateDiffHtml(@NonNull String diffSource, boolean isDark,
                                   @NonNull String backgroundColor, boolean wrap) {
        String skin = isDark ? "diff_dark.css" : "diff_light.css";
        return generateDiffHtml(diffSource, backgroundColor, skin, wrap);
    }

    private static String generateDiffHtml(@NonNull String diffSource, @NonNull String backgroundColor,
                                           String skin, boolean wrap) {
        return "<html>\n" +
                    "<head>\n" +
                        "<meta charset=\"utf-8\" />\n" +
                        "<title>Diff View</title>\n" +
                        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
                        "<link rel=\"stylesheet\" type=\"text/css\" href=\"./" + skin + "\">\n" +
                        "<style>" +
                            "body {background: " + backgroundColor + ";}\n" +
                            ".pre {\n" +
                                    "background: " + backgroundColor + ";\n" +
                                    " word-wrap: " + (wrap ? "break-word" : "normal") + ";\n" +
                                    " white-space: " + (wrap ? "pre-wrap" : "pre") + ";\n" +
                            "}\n" +
                        "</style>\n" +
                    "</head>\n" +

                    "<body>\n" +
                        "<pre class=\"pre\">\n" +
                            parseDiffSource(formatCode(diffSource), wrap) +
                        "</pre>\n" +
                    "</body>\n" +
                "</html>";
    }

    private static String parseDiffSource(@NonNull String diffSource, boolean wrap) {
        StringBuilder source = new StringBuilder();
        String[] lines = diffSource.split("\\n");

        int addStartLine = -1;
        int removeStartLine = -1;
        int addLineNum = 0;
        int removeLineNum = 0;
        int normalLineNum = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            String lineNumberStr = "";
            String classStr = "";
            int curAddNumber = -1;
            int curRemoveNumber = -1;

            if (line.startsWith("+")) {
                classStr = "class=\"add\";";
                curAddNumber = addStartLine + normalLineNum + addLineNum;
                addLineNum++;
            } else if (line.startsWith("-")) {
                classStr = "class=\"remove\";";
                curRemoveNumber = removeStartLine + normalLineNum + removeLineNum;
                removeLineNum++;
            } else if (line.startsWith("@@")) {
                classStr = "class=\"change\";";
                removeStartLine = getRemoveStartLine(line);
                addStartLine = getAddStartLine(line);
                addLineNum = 0;
                removeLineNum = 0;
                normalLineNum = 0;
            } else if (!line.startsWith("\\")) {
                curAddNumber = addStartLine + normalLineNum + addLineNum;
                curRemoveNumber = removeStartLine + normalLineNum + removeLineNum;
                normalLineNum++;
            }
            lineNumberStr = getDiffLineNumber(curRemoveNumber == -1 ? "" : String.valueOf(curRemoveNumber),
                    curAddNumber == -1 ? "" : String.valueOf(curAddNumber));

            source.append("\n")
                    .append("<div ").append(classStr).append(">")
                    .append(wrap ? "" : lineNumberStr + getBlank(1))
                    .append(line)
                    .append("</div>");
        }
        return source.toString();
    }

    private static int getRemoveStartLine(String line){
        try {
            return Integer.parseInt(line.substring(line.indexOf("-") + 1, line.indexOf(",")));
        } catch (Exception e){
            return 1;
        }
    }

    private static int getAddStartLine(String line){
        try {
            return Integer.parseInt(line.substring(line.indexOf("+") + 1,
                    line.indexOf(",", line.indexOf("+"))));
        } catch (Exception e){
            return 1;
        }
    }

    private static String getDiffLineNumber(String removeNumber, String addNumber){
        int minLength = 4;
        return getBlank(minLength - removeNumber.length()) +
                removeNumber +
                getBlank(1) +
                getBlank(minLength - addNumber.length()) +
                addNumber;
    }

    private static String getBlank(int num){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < num; i++){
            builder.append(" ");
        }
        return builder.toString();
    }

}
