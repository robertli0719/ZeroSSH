/*
 * Copyright 2016 Robert Li.
 * Released under the MIT license
 * https://opensource.org/licenses/MIT
 */
package robertli.zero.struts2;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.struts2.ServletActionContext;
import org.json.JSONObject;
import robertli.zero.action.file.ImageAction;
import robertli.zero.core.ImageService;
import robertli.zero.core.JsonService;

/**
 * This is a struts2 interceptor. This one will be use for packet
 * robertli.zero.action.file.ImageAction<br>
 *
 * This interceptor will preprocess image before ImageAction.upload(). <br>
 *
 * This interceptor need to be used after defaultStack.<br>
 *
 * @see robertli.zero.action.file.ImageAction
 * @version 1.0 2016-10-01
 * @author Robert Li
 */
public class ImagePreprocessInterceptor implements Interceptor {

    @Resource
    private JsonService jsonService;

    @Resource
    private ImageService imageService;

    @Override
    public void destroy() {
    }

    @Override
    public void init() {
    }

    private String validateUpload(File[] img, String[] imgContentType) {
        if (img == null || img.length == 0) {
            JSONObject result = jsonService.createFailResult("not found img in params");
            return result.toString();
        }
        for (String type : imgContentType) {
            if (type.startsWith("image/") == false) {
                JSONObject result = jsonService.createFailResult("wrong file type");
                return result.toString();
            }
        }
        for (File fe : img) {
            if (fe.length() > ImageAction.IMAGE_SIZE_LIMIT) {
                JSONObject result = jsonService.createFailResult("the file is bigger than the limit in ImageAction");
                return result.toString();
            }
        }
        return null;
    }

    private void compress(File[] img, String[] imgContentType) throws IOException {
        for (int i = 0; i < img.length; i++) {
            imgContentType[i] = "image/jpeg";
            BufferedImage image = imageService.readImage(img[i]);
            image = imageService.compress(image, ImageAction.MAX_WIDTH, ImageAction.MAX_HEIGHT);
            imageService.writeImage(img[i], image, "jpg");
        }
    }

    @Override
    public String intercept(ActionInvocation ai) throws Exception {
        if (ai.getAction() instanceof ImageAction == false) {
            return ai.invoke();
        }
        String method = ServletActionContext.getActionMapping().getMethod();
        if (method == null || method.equals("upload") == false) {
            return ai.invoke();
        }
        ImageAction action = (ImageAction) ai.getAction();
        String textResult = validateUpload(action.getImg(), action.getImgContentType());

        if (textResult != null) {
            Map<String, Object> context = new HashMap<>();
            context.put("textResult", textResult);
            ai.getStack().push(context);
            return ImageAction.TEXT;
        }
        compress(action.getImg(), action.getImgContentType());
        return ai.invoke();
    }

}
