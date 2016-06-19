/*
 * This file is part of Quelea, free projection software for churches.
 * 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.quelea.services.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;
import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.net.URL;
import org.javafx.dialog.Dialog;

/**
 * A very basic dropbox example.
 * @author mjrb5
 */
public class DropboxTest {

    private static final String APP_KEY = "op150kqkvln7q4t";
    private static final String APP_SECRET = "fjajunxibzifx7j";
    private static final AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
    private static DropboxAPI<WebAuthSession> mDBApi;

    public static void main(String[] args) throws Exception {
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
        WebAuthInfo authInfo = session.getAuthInfo();

        RequestTokenPair pair = authInfo.requestTokenPair;
        String url = authInfo.url;

        Desktop.getDesktop().browse(new URL(url).toURI());
        Dialog.showInfo("Press ok to continue once you have authenticated.", "Authentication");
        session.retrieveWebAccessToken(pair);

        AccessTokenPair tokens = session.getAccessTokenPair();
        System.out.println("Use this token pair in future so you don't have to re-authenticate each time:");
        System.out.println("Key token: " + tokens.key);
        System.out.println("Secret token: " + tokens.secret);

        mDBApi = new DropboxAPI<>(session);

        System.out.println();
        System.out.print("Uploading file...");
        String fileContents = "Hello World!";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes("UTF8"));
        Entry newEntry = mDBApi.putFile("/testing.txt", inputStream, fileContents.length(), null, null);
        System.out.println("Done. \nRevision of file: " + newEntry.rev);
        
    }
}
