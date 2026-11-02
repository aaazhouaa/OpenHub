

package com.thirtydegreesray.openhub.ui.widget.webview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.thirtydegreesray.openhub.mvp.model.GitHubName;
import com.thirtydegreesray.openhub.util.AppUtils;
import com.thirtydegreesray.openhub.util.GitHubHelper;
import com.thirtydegreesray.openhub.util.StringUtils;

import java.util.ArrayList;
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
    //the lookbehind keeps attributes such as data-src or data-canonical-src out of the match
    private static final Pattern IMAGE_PATTERN = Pattern.compile("(?<![-\\w])src=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);
    // <source srcset="..."> 用于 <picture> 响应式图片（暗色/亮色切换）
    private static final Pattern SRCSET_PATTERN = Pattern.compile("srcset=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);

    static String generateImageHtml(@NonNull String imageUrl, @NonNull String backgroundColor) {
        boolean isDark = AppUtils.isNightMode();
        String imgFilter = isDark ? "opacity:0.70;filter:brightness(0.70);" : "";
        return "<!DOCTYPE html><html><head>" +
                "<meta charset=\"utf-8\">" +
                "<meta name=\"referrer\" content=\"no-referrer\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">" +
                "<style>" +
                "html,body{margin:0;padding:0;width:100%;height:100%;background:" + backgroundColor + ";overflow:hidden;touch-action:none;}" +
                "#container{position:relative;width:100%;height:100%;display:flex;align-items:center;justify-content:center;overflow:hidden;}" +
                "#img{max-width:100%;max-height:100%;transform-origin:center center;will-change:transform;" + imgFilter + "}" +
                "</style></head><body>" +
                "<div id=\"container\"><img id=\"img\" src=\"" + imageUrl + "\"></div>" +
                "<script>" +
                // 只缩放图片元素，背景保持不动
                "(function(){" +
                "var img=document.getElementById('img'),c=document.getElementById('container');" +
                "var scale=1,tx=0,ty=0,startScale=1,startDist=0,startX=0,startY=0,startTx=0,startTy=0,panning=false,edgeNotified=false;" +
                "function notify(){try{if(window.OpenHubZoom){OpenHubZoom.onScale(scale);}}catch(e){}}" +
                "function notifyEdge(dir){edgeNotified=true;try{if(window.OpenHubZoom){OpenHubZoom.onPanEdge(dir);}}catch(e){}}" +
                "function clamp(){var iw=img.clientWidth*scale,ih=img.clientHeight*scale;" +
                "var maxTx=Math.max(0,(iw-c.clientWidth)/2),maxTy=Math.max(0,(ih-c.clientHeight)/2);" +
                "tx=Math.max(-maxTx,Math.min(maxTx,tx));ty=Math.max(-maxTy,Math.min(maxTy,ty));}" +
                "function apply(){img.style.transform='translate('+tx+'px,'+ty+'px) scale('+scale+')';notify();}" +
                "function dist(t){var dx=t[0].clientX-t[1].clientX,dy=t[0].clientY-t[1].clientY;return Math.sqrt(dx*dx+dy*dy);}" +
                "document.addEventListener('touchstart',function(e){" +
                "if(e.touches.length===2){startDist=dist(e.touches);startScale=scale;panning=false;}" +
                "else if(e.touches.length===1&&scale>1){panning=true;edgeNotified=false;startX=e.touches[0].clientX;startY=e.touches[0].clientY;startTx=tx;startTy=ty;}" +
                "},{passive:true});" +
                "document.addEventListener('touchmove',function(e){" +
                "if(e.touches.length===2){e.preventDefault();var d=dist(e.touches);" +
                "if(startDist>0){scale=Math.max(1,Math.min(5,startScale*d/startDist));clamp();apply();}}" +
                "else if(e.touches.length===1&&panning&&scale>1){e.preventDefault();" +
                "var nx=startTx+(e.touches[0].clientX-startX),ny=startTy+(e.touches[0].clientY-startY);" +
                // 拖到水平边界后继续拖，交还给外层翻页
                "if(!edgeNotified){var mx=Math.max(0,(img.clientWidth*scale-c.clientWidth)/2);" +
                "if(nx<-mx-40){notifyEdge(1);}else if(nx>mx+40){notifyEdge(-1);}}" +
                "tx=nx;ty=ny;clamp();apply();}" +
                "},{passive:false});" +
                "document.addEventListener('touchend',function(e){" +
                "if(e.touches.length<2)startDist=0;" +
                "if(e.touches.length===0){panning=false;if(scale<1.05){scale=1;tx=0;ty=0;apply();}}" +
                "});" +
                "})();" +
                "</script></body></html>";
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
        String baseDir = "";
        if (baseUrl.contains("blob/")) {
            int blobIndex = baseUrl.indexOf("blob/") + 5;
            int branchEnd = baseUrl.indexOf("/", blobIndex);
            if (branchEnd != -1) {
                branch = baseUrl.substring(blobIndex, branchEnd);
                String path = baseUrl.substring(branchEnd + 1);
                int lastSlash = path.lastIndexOf('/');
                baseDir = lastSlash == -1 ? "" : path.substring(0, lastSlash + 1);
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

            String cleanUrl = resolveRelativeUrl(baseDir, oriUrl);
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
            String attr = imagesMatcher.group(1);
            String fixed = fixImageUrl(attr, owner, repo, branch, baseDir);
            if (fixed == null) continue;
            source = source.replace(imagesMatcher.group(0), "src=\"" + fixed + "\"");
        }

        // <picture> 响应式图片的 <source srcset> 使用暗色/适配变体，需同样转换，
        // 否则暗色模式下走 srcset 的相对路径会解析失败。
        Matcher srcsetMatcher = SRCSET_PATTERN.matcher(source);
        while (srcsetMatcher.find()) {
            String attr = srcsetMatcher.group(1);
            String fixed = fixImageUrl(attr, owner, repo, branch, baseDir);
            if (fixed == null) continue;
            source = source.replace(srcsetMatcher.group(0), "srcset=\"" + fixed + "\"");
        }

        return source;
    }

    /**
     * Normalizes a README-relative image URL (used by both src and srcset) into an
     * absolute raw.githubusercontent.com URL. Returns null when no change is needed.
     */
    @Nullable
    private static String fixImageUrl(@NonNull String oriUrl, @NonNull String owner,
                                      @NonNull String repo, @NonNull String branch,
                                      @NonNull String baseDir) {
        if (StringUtils.isBlank(oriUrl)) return null;

        // GitHub 网页 URL（blob 或 raw 视图）返回的是 HTML 页面，作为 img src 必定无法渲染，
        // 转换为 raw.githubusercontent.com 原始文件地址，并规范化反斜杠引入的双斜杠。
        if (oriUrl.contains("github.com")
                && (oriUrl.contains("/blob/") || oriUrl.contains("/raw/"))) {
            String fixed = oriUrl.replace('\\', '/')
                    .replace("github.com", "raw.githubusercontent.com")
                    .replace("/blob/", "/")
                    .replace("/raw/", "/");
            return normalizePathSlashes(fixed);
        }

        // 已经是完整网络链接
        if (oriUrl.startsWith("http://") || oriUrl.startsWith("https://")) {
            return null;
        }
        if (oriUrl.startsWith("//")) {
            return "https:" + oriUrl;
        }

        String cleanUrl = resolveRelativeUrl(baseDir, oriUrl);
        // 已含仓库前缀的 GitHub raw 视图路径：/owner/repo/raw/{branch}/{path}
        // 需剥离 /raw/ 前缀，并规范化反斜杠转换引入的多余斜杠
        if (cleanUrl.startsWith("/" + owner + "/" + repo + "/raw/") ||
                cleanUrl.startsWith(owner + "/" + repo + "/raw/")) {
            String sub = cleanUrl.startsWith("/") ? cleanUrl : "/" + cleanUrl;
            String rawPrefix = "/" + owner + "/" + repo + "/raw/";
            String path = sub.substring(rawPrefix.length()).replaceAll("/+", "/");
            return "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + path;
        }
        if (cleanUrl.startsWith("/" + owner + "/" + repo + "/blob/") ||
                cleanUrl.startsWith(owner + "/" + repo + "/blob/")) {
            String sub = cleanUrl.startsWith("/") ? cleanUrl : "/" + cleanUrl;
            return "https://raw.githubusercontent.com" + sub.replace("/blob/", "/");
        }

        String subUrl = cleanUrl.startsWith("/") ? cleanUrl : "/" + cleanUrl;
        return "https://raw.githubusercontent.com/" + owner + "/" + repo + "/" + branch + subUrl;
    }

    /**
     * Collapses duplicate slashes in the path portion of a URL, leaving the scheme's
     * "://" intact. e.g. "https://x/main//imgs/icon.webp" -> "https://x/main/imgs/icon.webp".
     */
    @NonNull
    private static String normalizePathSlashes(@NonNull String url) {
        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) return url.replaceAll("/+", "/");
        String scheme = url.substring(0, schemeEnd + 3);
        String rest = url.substring(schemeEnd + 3).replaceAll("/+", "/");
        return scheme + rest;
    }

    /**
     * Resolves a repository-relative link against the folder holding the page, so
     * links such as ../img/x.png of a nested README point at the right file.
     */
    @NonNull
    private static String resolveRelativeUrl(@NonNull String baseDir, @NonNull String url) {
        // Some README files use Windows-style backslashes (e.g. <img src="\imgs\icon.webp">),
        // which are invalid in a URL and break the image. Normalize them to forward slashes.
        String res = url.trim().replace('\\', '/');
        if (res.startsWith("/") || res.contains("://")) return res;
        ArrayList<String> parts = new ArrayList<>();
        for (String segment : baseDir.split("/")) {
            if (!segment.isEmpty()) parts.add(segment);
        }
        for (String segment : res.split("/")) {
            if (segment.isEmpty() || ".".equals(segment)) continue;
            if ("..".equals(segment)) {
                if (!parts.isEmpty()) parts.remove(parts.size() - 1);
            } else {
                parts.add(segment);
            }
        }
        return TextUtils.join("/", parts);
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
