/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Raymond Kroon 
 *
 */

package org.geowebcache.rest.versioning;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.geowebcache.rest.GWCRestlet;
import org.geowebcache.rest.RestletException;
import org.geowebcache.storage.versioned.VersionRepository;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;

/**
 *  Versioning restlet
 */
public class LayerVersionRestlet extends GWCRestlet {
    
    private VersionRepository versionRepository;
    
    public void setVersionRepository(VersionRepository versionRepository) {
        this.versionRepository = versionRepository;
    }
    
    public void handle(Request request, Response response) {
        Method met = request.getMethod();
        if (met.equals(Method.POST)) {
            doPost(request, response);
        }
        else if (met.equals(Method.GET)) {
            doGet(request, response);
        }
        else {
            throw new RestletException("Method not allowed",
                    Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
        }
    }
    
    public void doGet(Request request, Response response) {
        
        String layerName = getLayerName(request);
        long version = versionRepository.getVersion(layerName);
        
        JSONObject obj = new JSONObject(new LayerVersionDto(layerName, version));

        Representation rep = new JsonRepresentation(obj);
        response.setEntity(rep);
    }
    
    public void doPost(Request request, Response response) {
        String layerName = getLayerName(request);
        try {
            JSONObject requestObj = new JSONObject(request.getEntity().getText());
            if (!layerName.equals(requestObj.getString("layerName"))) {
                throw new RestletException("layerNames should match", Status.CLIENT_ERROR_BAD_REQUEST);
            }
            
            long version = requestObj.getLong("version");
            
            versionRepository.setVersion(layerName, version);
            
            JSONObject responseObj = new JSONObject(new LayerVersionUpdateResponse("SUCCEED", null));
            Representation rep = new JsonRepresentation(responseObj);
            
            response.setEntity(rep);
        } catch (IOException | JSONException ex) {
            JSONObject obj = new JSONObject(new LayerVersionUpdateResponse("ERROR", ex.getMessage()));
            Representation rep = new JsonRepresentation(obj);
            
            response.setEntity(rep);
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }
    
    private String getLayerName(Request request) {
        final String layerName;
        if (request.getAttributes().containsKey("layer")) {
            try {
                layerName = URLDecoder.decode((String) request.getAttributes().get("layer"), "UTF-8");
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException(uee);
            }
        } else {
            layerName = null;
        }
        
        return layerName;
    }
}
