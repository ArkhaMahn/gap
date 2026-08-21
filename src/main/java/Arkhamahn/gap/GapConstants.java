package Arkhamahn.gap;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Ported constants from GAP.py for the ZAP add-on.
 *
 * <p>Original GAP by xnl-h4ck3r (@xnl_h4ck3r).
 */
public final class GapConstants {

    public static final String VERSION = "1.0.0";
    public static final boolean DEBUG = false;

    // URLS
    public static final String GAP_HELP_URL =
            "https://github.com/xnl-h4ck3r/GAP-Burp-Extension/blob/main/GAP%20Help.md";
    public static final String GAP_HELP_URL_BUTTON =
            "https://raw.githubusercontent.com/xnl-h4ck3r/GAP-Burp-Extension/main/GAP%20Help.md";
    public static final String GAP_HELP_404 =
            "<h1>Oops... mind the GAP!</h1><p>Sorry, this should be displaying the content of the following page:<p><a href="
                    + GAP_HELP_URL
                    + ">"
                    + GAP_HELP_URL
                    + "</a><p>However, there seems to be a problem connecting to that resource.<p>Please try again later. If the problem persists, please raise an issue on Github.";
    public static final String HELP_ICON =
            "https://cdn0.iconfinder.com/data/icons/simply-orange-1/128/questionssvg-512.png";
    public static final String DIR_ICON =
            "https://cdn0.iconfinder.com/data/icons/simply-orange-1/128/currency_copysvg-512.png";
    public static final String URL_GAP_LOGO =
            "https://github.com/xnl-h4ck3r/GAP-Burp-Extension/raw/main/GAP/images/banner.png";
    public static final String URL_KOFI = "https://ko-fi.com/B0B3CZKR5";
    public static final String URL_KOFI_BUTTON = "https://storage.ko-fi.com/cdn/kofi2.png?v=3";
    public static final String URL_GITHUB = "https://github.com/xnl-h4ck3r";

    public static final Color COLOR_LIGHT_BLUE = new Color(0x5B9BD5); // softer sky blue (was 0x1E90FF)

    public static final Color COLOR_OUTPUT_BG = new Color(0xE0E0E0);

    // Enumeration of request parameter types identified by Burp (kept for parity)
    public static final int PARAM_URL = 0;
    public static final int PARAM_BODY = 1;
    public static final int PARAM_COOKIE = 2;
    public static final int PARAM_XML = 3;
    public static final int PARAM_XML_ATTR = 4;
    public static final int PARAM_MULTIPART_ATTR = 5;
    public static final int PARAM_JSON = 6;

    public static final String DEFAULT_MAX_WORD_LEN = "40";
    public static final String OLD_DEFAULT_LINK_PREFIX = "https://www.CHANGE.THIS";
    public static final String DEFAULT_LINK_PREFIX = "https://CHANGE.THIS";
    public static final String DEFAULT_QSV = "XNLV";
    public static final String DEFAULT_WARNING_NO_CONTENT =
            "\n\nMaybe scope isn't set?\nIt needs to be set to call GAP from the Site Map tree.\nIgnore this if there are results for other modes.";

    public static final int DEFAULT_REGEX_TIMEOUT = 30;
    public static final int LARGE_RESPONSE_THRESHOLD = 50000;
    public static final int CHUNK_SIZE = 40000;
    public static final int CHUNK_OVERLAP = 5000;

    public static final String LINK_REGEX_FILES =
            "php|php3|php5|asp|aspx|ashx|cfm|cgi|pl|jsp|jspx|json|js|action|html|xhtml|htm|bak|do|txt|wsdl|wadl|xml|xls|xlsx|bin|conf|config|bz2|bzip2|gzip|tar\\.gz|tgz|log|src|zip|js\\.map";

    public static final String DEFAULT_WORDS_CONTENT_TYPES =
            "text/html,application/xml,application/json,text/plain,application/xhtml+xml,application/ld+json,text/xml";

    public static final String DEFAULT_EXCLUSIONS =
            ".css,.jpg,.jpeg,.png,.svg,.img,.gif,.mp4,.flv,.ogv,.webm,.webp,.mov,.mp3,.m4a,.m4p,.scss,.tif,.tiff,.ttf,.otf,.woff,.woff2,.bmp,.ico,.eot,.htc,.rtf,.swf,.image,w3.org,doubleclick.net,youtube.com,.vue,jquery,bootstrap,font,jsdelivr.net,vimeo.com,pinterest.com,facebook,linkedin,twitter,instagram,google,mozilla.org,jibe.com,schema.org,schemas.microsoft.com,wordpress.org,w.org,wix.com,parastorage.com,whatwg.org,polyfill,typekit.net,schemas.openxmlformats.org,openweathermap.org,openoffice.org,reactjs.org,angularjs.org,java.com,purl.org,/image,/img,/css,/wp-json,/wp-content,/wp-includes,/theme,/audio,/captcha,/font,node_modules,.wav,.gltf,.pict,.svgz,.eps,.midi,.mid,.avif,.jfi,.jfif,.jfif-tbnl,.jif,.jpe,.pjpg";

    public static final String CONTENTTYPE_EXCLUSIONS =
            "text/css,image/jpeg,image/jpg,image/png,image/svg+xml,image/gif,image/tiff,image/webp,image/bmp,image/x-icon,image/vnd.microsoft.icon,font/ttf,font/woff,font/woff2,font/x-woff2,font/x-woff,font/otf,audio/mpeg,audio/wav,audio/webm,audio/aac,audio/ogg,audio/wav,audio/webm,video/mp4,video/mpeg,video/webm,video/ogg,video/mp2t,video/webm,video/x-msvideo,application/font-woff,application/font-woff2,application/vnd.android.package-archive,binary/octet-stream,application/octet-stream,application/pdf,application/x-font-ttf,application/x-font-otf,application/x-font-woff,application/vnd.ms-fontobject,image/avif,application/zip,application/x-zip-compressed,application/x-msdownload,application/x-apple-diskimage,application/x-rpm,application/vnd.debian.binary-package,application/x-font-truetype,font/opentype,image/pjpeg,application/x-troff-man,application/font-otf,application/x-ms-application,application/x-msdownload,video/x-ms-wmv,image/x-png,video/quicktime,image/x-ms-bmp,font/opentype,application/x-font-opentype,application/x-woff,audio/aiff,image/jp2,video/x-m4v";

    public static final String FILEEXT_EXCLUSIONS =
            ".zip,.dmg,.rpm,.deb,.gz,.tar,.jpg,.jpeg,.png,.svg,.img,.gif,.mp4,.flv,.ogv,.webm,.webp,.mov,.mp3,.m4a,.m4p,.scss,.tif,.tiff,.ttf,.otf,.woff,.woff2,.bmp,.ico,.eot,.htc,.rtf,.swf,.image,.wav,.gltf,.pict,.svgz,.eps,.midi,.mid,.pdf,.jfi,.jfif,.jfif-tbnl,.jif,.jpe,.pjpg";

    public static final String DEFAULT_STOP_WORDS =
            "a,aboard,about,above,across,after,afterwards,again,against,all,almost,alone,along,already,also,although,always,am,amid,among,amongst,an,and,another,any,anyhow,anyone,anything,anyway,anywhere,are,around,as,at,back,be,became,because,become,becomes,becoming,been,before,beforehand,behind,being,below,beneath,beside,besides,between,beyond,both,bottom,but,by,can,cannot,cant,con,concerning,considering,could,couldnt,cry,de,describe,despite,do,done,down,due,during,each,eg,eight,either,eleven,else,elsewhere,empty,enough,etc,even,ever,every,everyone,everything,everywhere,except,few,fifteen,fifty,fill,find,fire,first,five,for,former,formerly,forty,found,four,from,full,further,get,give,go,had,has,hasnt,have,he,hence,her,here,hereafter,hereby,herein,hereupon,hers,herself,him,himself,his,how,however,hundred,i,ie,if,in,inc,indeed,inside,interest,into,is,it,its,itself,keep,last,latter,latterly,least,less,like,ltd,made,many,may,me,meanwhile,might,mill,mine,more,moreover,most,mostly,move,much,must,my,myself,name,namely,near,neither,never,nevertheless,next,nine,no,nobody,none,noone,nor,not,nothing,now,nowhere,of,off,often,on,once,one,only,onto,or,other,others,otherwise,our,ours,ourselves,out,outside,over,own,part,past,per,perhaps,please,put,rather,re,regarding,round,same,see,seem,seemed,seeming,seems,serious,several,she,should,show,side,since,sincere,six,sixty,so,some,somehow,someone,something,sometime,sometimes,somewhere,still,such,take,ten,than,that,the,their,them,themselves,then,thence,there,thereafter,thereby,therefore,therein,thereupon,these,they,thick,thin,third,this,those,though,three,through,throughout,thru,thus,to,together,too,top,toward,towards,twelve,twenty,two,un,under,underneath,until,unto,up,upon,us,very,via,want,was,wasnt,we,well,went,were,weve,what,whatever,when,whence,whenever,where,whereafter,whereas,whereby,wherein,whereupon,wherever,whether,which,while,whilst,whither,whoever,whole,whom,whose,why,will,with,within,without,would,yet,you,youll,your,yours,yourself,yourselves";

    // Common domain TLDS
    public static final Set<String> COMMON_TLDS =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "com", "de", "net", "org", "uk", "cn", "ga", "nl", "cf", "ml", "tk",
                            "ru", "br", "gq", "xyz", "fr", "eu", "info", "co", "au", "ca", "it",
                            "in", "ch", "pl", "es", "online", "us", "top", "jp", "biz", "se", "at",
                            "dk", "cz", "za", "me", "ir", "icu", "shop", "kr", "site", "mx", "hu",
                            "io", "cc", "club", "no", "cyou", "store"));

    public static final Set<String> SUS_CMDI =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "execute", "dir", "daemon", "cli", "log", "cmd", "download", "ip",
                            "upload", "message", "input_file", "format", "expression", "data",
                            "bsh", "bash", "shell", "command", "range", "sort", "host", "exec",
                            "code"));

    public static final Set<String> SUS_DEBUG =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "test", "reset", "config", "shell", "admin", "exec", "load", "cfg",
                            "dbg", "edit", "root", "create", "access", "disable", "alter", "make",
                            "grant", "adm", "toggle", "execute", "clone", "delete", "enable",
                            "rename", "debug", "modify", "stacktrace"));

    public static final Set<String> SUS_FILEINC =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "root", "directory", "path", "style", "folder", "default-language",
                            "url", "platform", "textdomain", "document", "template", "pg",
                            "php_path", "doc", "type", "lang", "token", "name", "pdf", "file",
                            "etc", "api", "app", "resource-type", "controller", "filename", "page",
                            "f", "view", "input_file"));

    public static final Set<String> SUS_IDOR =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "count", "key", "user", "id", "extended_data", "uid2", "group",
                            "team_id", "data-id", "no", "username", "email", "account", "doc",
                            "uuid", "profile", "number", "user_id", "edit", "report", "order"));

    public static final Set<String> SUS_OPENREDIRECT =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "u", "redirect_uri", "failed", "r", "referer", "return_url",
                            "redirect_url", "prejoin_data", "continue", "redir", "return_to",
                            "origin", "redirect_to", "next"));

    public static final Set<String> SUS_SQLI =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "process", "string", "id", "referer", "password", "pwd", "field",
                            "view", "sleep", "column", "log", "token", "sel", "select", "sort",
                            "from", "search", "update", "pub_group_id", "row", "results", "role",
                            "table", "multi_layer_map_list", "order", "filter", "params", "user",
                            "fetch", "limit", "keyword", "email", "query", "c", "name", "where",
                            "number", "phone_number", "delete", "report", "q", "sql"));

    public static final Set<String> SUS_SSRF =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "sector_identifier_uri", "request_uris", "logo_uri", "jwks_uri",
                            "start", "path", "domain", "source", "url", "site", "view", "template",
                            "page", "show", "val", "dest", "metadata", "out", "feed", "navigation",
                            "image_host", "uri", "next", "continue", "host", "window", "dir",
                            "reference", "filename", "html", "to", "return", "open", "port",
                            "stop", "validate", "resturl", "callback", "name", "data", "ip",
                            "redirect", "target", "referer"));

    public static final Set<String> SUS_SSTI =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "preview", "activity", "id", "name", "content", "view", "template",
                            "redirect"));

    public static final Set<String> SUS_XSS =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "path", "admin", "class", "atb", "redirect_uri", "other", "utm_source",
                            "currency", "dir", "title", "endpoint", "return_url", "users",
                            "cookie", "state", "callback", "militarybranch", "e", "referer",
                            "password", "author", "body", "status", "utm_campaign", "value", "text",
                            "search", "flaw", "vote", "pathname", "params", "user", "t",
                            "utm_medium", "q", "email", "what", "file", "data-original",
                            "description", "subject", "action", "u", "nickname", "color",
                            "language_id", "auth", "samlresponse", "return", "readyfunction",
                            "where", "tags", "cvo_sid1", "target", "format", "back", "term", "r",
                            "id", "url", "view", "username", "sequel", "type", "city", "src", "p",
                            "label", "ctx", "style", "html", "ad_type", "s", "issues", "query",
                            "c", "shop", "redirect", "page", "prefv1", "destination", "mode",
                            "data", "error", "editor", "wysiwyg", "widget", "msg"));

    public static final Set<String> SUS_MASSASSIGNMENT =
            new HashSet<>(
                    java.util.Arrays.asList(
                            "user", "profile", "role", "settings", "data", "attributes", "post",
                            "comment", "order", "product", "form_fields", "request"));

    // Compiled regexes (ported from GAP.py registerExtenderCallbacks)

    public static String buildLinkRegexNonStandardFiles() {
        StringBuilder sb = new StringBuilder();
        String[] exts = LINK_REGEX_FILES.split("\\|");
        for (String ext : exts) {
            if (ext.length() > 4 || ext.matches(".*\\d.*")) {
                if (sb.length() == 0) {
                    sb.append(ext);
                } else {
                    sb.append("|").append(ext);
                }
            }
        }
        return sb.toString();
    }

    public static final Pattern REGEX_LINKS =
            Pattern.compile(
                    "(?:(?<=^)|(?<=\"|'|\\n|\\r|\\s))(((?:[a-zA-Z]{1,10}:\\/\\/|\\/\\/)([^\"'/\\s]{1,255}\\.[a-zA-Z]{2,24}|localhost)[^\"'\\n\\s]{0,255})|((?:#?\\/|\\.\\.\\/|\\.\\/)[^\"'><,;| *()(%%$^\\/\\\\\\[\\]][^\"'><,;|()\\s]{1,255})|([a-zA-Z0-9_\\-\\/]{1,}\\/[a-zA-Z0-9_\\-\\/\\.]{1,255}\\.(?:[a-zA-Z]{1,4}"
                            + buildLinkRegexNonStandardFiles()
                            + ")(?:[\\?|\\/][^\"|']{0,1000}|))|([a-zA-Z0-9_\\-\\.]{1,255}\\.(?:"
                            + LINK_REGEX_FILES
                            + ")(?:\\?[^\"|^']{0,255}|)))(?=$|\"|'|\\n|\\r|\\s)|(?<=^Disallow:\\s)[^$\\n]{0,500}|(?<=^Allow:\\s)[^$\\n]{0,500}|(?<= Domain\\=)[^\";']{0,500}|(?<=<)https?:\\/\\/[^>\\n]{0,1000}|(\"|')([A-Za-z0-9_-]+\\/)+[A-Za-z0-9_-]+(\\.[A-Za-z0-9]{2,}|\\/?(\\?|\\#)[A-Za-z0-9_\\-&=\\[\\]]{0,500})(\"|')|(?<=<Key>)[^<]{1,500}<\\/Key>",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_LINKS_EXTRA =
            Pattern.compile(
                    "(?:[a-zA-Z0-9%\\u0080-\\uFFFF_-]+\\.){0,5}[a-zA-Z0-9%\\u0080-\\uFFFF_-]+\\.[a-zA-Z]{2,24}(?:\\/[^\\s\"'<>()\\[\\]{}]{0,500})?",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_LINKS_JSBUILT =
            Pattern.compile(
                    "\\.(?:get|post|put|delete|patch)\\(\\s*[\"'`]([^)]{0,1000}?)\\)",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_LINKS_FETCH =
            Pattern.compile(
                    "fetch\\s*\\(\\s*[\"'`]((?:\\/|https?:\\/\\/)[^\"'`)]{0,1000})[\"'`]",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_BURPURL =
            Pattern.compile(
                    "^(https?:)?\\/\\/([-a-zA-Z0-9_]+\\.)?[-a-zA-Z0-9_]+\\.[-a-zA-Z0-9_\\.\\?\\#\\&\\=]+$",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_JSONKEYS =
            Pattern.compile("\"([a-zA-Z0-9$_\\.-]{0,1000}?)\":");

    public static final Pattern REGEX_XMLATTR = Pattern.compile("<([a-zA-Z0-9$_\\.-]{0,1000}?)>");

    public static final Pattern REGEX_XMLDATA = Pattern.compile("<[^<>]*>([^<>]{1,1000}?)</[^<>]*>");

    public static final Pattern REGEX_HTMLINP =
            Pattern.compile("<(input|textarea|select|button)(.*?)>", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_HTMLINP_NAME =
            Pattern.compile(
                    "(?<=\\sname)[\\s]{0,10}\\=[\\s]{0,10}(\"|')(.{0,1000}?)(?=(\"|'))",
                    Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_HTMLINP_ID =
            Pattern.compile(
                    "(?<=\\sid)[\\s]{0,10}\\=[\\s]{0,10}(\"|')(.{0,1000}?)(?=(\"|'))",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_SOURCEMAP =
            Pattern.compile("(?<=SourceMap\\:\\s).{0,1000}?(?=\\n)", Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_WORDS = Pattern.compile("(?<![\\/])\\b\\w{3,}\\b(?![\\/])");
    public static final Pattern REGEX_WORDSUB =
            Pattern.compile(
                    "\"|%22|<|%3c|>|%3e|\\(|%28|\\)|%29|\\s|%20", Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_PORT80 = Pattern.compile(":80[^0-9]");
    public static final Pattern REGEX_PORT443 = Pattern.compile(":443[^0-9]");
    public static final Pattern REGEX_PORTSUB = Pattern.compile(":80[^0-9]|:443[^0-9]");
    public static final Pattern REGEX_PORTSUB80 = Pattern.compile(":80");
    public static final Pattern REGEX_PORTSUB443 = Pattern.compile(":443");

    public static final Pattern REGEX_PARAM = Pattern.compile("^[A-Za-z0-9_.~\\-\\[\\]]+$");
    public static final Pattern REGEX_MULTIPARTNAME = Pattern.compile("(?i)name=\"([^\"]*)\"");

    public static final Pattern REGEX_PARAMKEYS =
            Pattern.compile("(?<=\\?|&)[^\\=&\\n].{0,1000}?(?==|&|\\n)");

    public static final Pattern REGEX_PARAMSPOSSIBLE =
            Pattern.compile(
                    "(?<=[^&|%26|&amp;|&#0?38;|\\u0026|\\u0026|\\\\u0026|\\x26|\\x26])(\\?|%3f|&#0?63;|\\u003f|\\u003f|\\\\u003f|&|%26|&amp;|&#0?38;|\\u0026|\\u0026|\\\\u0026|\\x26|%3d|&#0?61;|\\u003d|\\u003d|\\\\u003d|\\x3d|&quot;|&#0?34;|\\u0022|\\u0022|\\\\u0022|&#0?39;)[a-z0-9_\\-]{3,}(=|%3d|&#0?61;|\\u003d|\\u003d|\\\\u003d|\\x3d|\\x3d)(?=[^=|%3d|&#0?61;|\\u003d|\\u003d|\\\\u003d|\\x3d|\\x3d])",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_PARAMSSUB =
            Pattern.compile(
                    "\\?|%3f|&#0?63;|\\u003f|\\u003f|\\\\u003f|=|%3d|&#0?61;|\\u003d|\\u003d|\\\\u003d|\\x3d|\\x3d|%26|&amp;|&#0?38;|\\u0026|\\u0026|\\\\u0026|\\x26|\\x26|&quot;|&#0?34;|\\u0022|\\u0022|\\\\u0022|\\x22|\\x22|&#0?39;",
                    Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_JSLET =
            Pattern.compile("(?<=let[\\s])[\\s]{0,10}[a-zA-Z$_][a-zA-Z0-9$_]{0,1000}[\\s]{0,10}(?=(=|;|\\n|\\r))");
    public static final Pattern REGEX_JSVAR =
            Pattern.compile("(?<=var\\s)[\\s]{0,10}[a-zA-Z$_][a-zA-Z0-9$_]{0,1000}?(?=(\\s|=|,|;|\\n))");
    public static final Pattern REGEX_JSCONSTS =
            Pattern.compile("(?<=const\\s)[\\s]{0,10}[a-zA-Z$_][a-zA-Z0-9$_]{0,1000}?(?=(\\s|=|,|;|\\n))");
    public static final Pattern REGEX_JSNESTED =
            Pattern.compile(
                    "(?s)(^|\\s?)(JSON\\.stringify\\(|dataLayer\\.push\\(|(var|let|const)\\s{1,10}[\\$A-Za-z0-9-_\\[\\]]{1,1000}\\s{0,10}=)\\s{0,10}\\{");
    public static final Pattern REGEX_JSNESTEDPARAM =
            Pattern.compile("\\s{0,10}('|\"|\\[])?[A-Za-z0-9-_\\.]{1,1000}('|\"|\\])?\\s{0,10}\\:");

    public static final Pattern REGEX_PARAMSJSON = Pattern.compile("\\{\"[^\\}]+\\}");
    public static final Pattern REGEX_PARAMSJSONPARAMS = Pattern.compile("(?<=\")[^\"\\:]+(?=\":)");

    public static final Pattern REGEX_LINKSSLASH =
            Pattern.compile("(\\&#x2f;|\\&#0?2f|%2f|\\u002f|\\u002f|\\/)", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_LINKSCOLON =
            Pattern.compile("(\\&#x3a;|\\&#0?3a|%3a|\\u003a|\\u003a)", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_LINKSAND =
            Pattern.compile("%26|&amp;|&#0?38;|\\u0026|u0026|x26|\\x26", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_LINKSEQUAL =
            Pattern.compile("%3d|&equals;|&#0?61;|\\u003d|u003d|x3d|\\x3d", Pattern.CASE_INSENSITIVE);
    public static final Pattern REGEX_LINKBRACKET = Pattern.compile("\\(.*\\)");
    public static final Pattern REGEX_LINKBRACES = Pattern.compile("\\{.*\\}");
    public static final Pattern REGEX_LINKSEARCH4 = Pattern.compile("<\\/");
    public static final Pattern REGEX_VALIDHOST =
            Pattern.compile("^([A-Za-z0-9_-]{1,100}\\.){1,100}[A-Za-z0-9_-]{2,}$");

    public static final Pattern REGEX_SUSPARAM = Pattern.compile("^[A-Za-z0-9_-]{1,500}$");

    public static final Pattern REGEX_CONTENTTYPE = Pattern.compile("Content-Type:[^\\r|\\n|$]*", Pattern.CASE_INSENSITIVE);

    public static final Pattern REGEX_BIGBASE64 = Pattern.compile("eyJ[a-zA-Z0-9\\+/]+(?:=|\\b|\\n)");

    public static final Pattern LINK_PREFIX_VALIDATOR =
            Pattern.compile(
                    "^https?:\\/\\/([-a-z0-9@:%._\\+~#=]{1,256}\\.)+[a-z0-9]{2,6}$",
                    Pattern.CASE_INSENSITIVE);

    private GapConstants() {}
}