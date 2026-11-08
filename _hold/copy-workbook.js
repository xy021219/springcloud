const fs = require("fs");
const src = "D:/ProjectFile/sheet-editor/_hold/java/sheet/SheetWorkbook.java";
const dst = "D:/ProjectFile/sheet-editor/_upstream/RuoYi-Cloud/ruoyi-modules/ruoyi-tool/src/main/java/com/ruoyi/web/core/sheet/SheetWorkbook.java";
let text = fs.readFileSync(src, "utf8");
text = text.replace("import com.ruoyi.common.exception.ServiceException;", "import com.ruoyi.common.core.exception.ServiceException;");
text = text.replace("import com.ruoyi.common.utils.StringUtils;", "import com.ruoyi.common.core.utils.StringUtils;");
fs.writeFileSync(dst, text, "utf8");
if (!text.includes("com.ruoyi.common.core.exception.ServiceException")) {
  throw new Error("import not updated");
}
if (!text.includes("SELECT_COLUMNS")) {
  throw new Error("missing rule");
}
