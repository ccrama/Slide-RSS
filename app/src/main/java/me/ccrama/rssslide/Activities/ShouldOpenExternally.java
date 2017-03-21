package me.ccrama.rssslide.Activities;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import me.ccrama.rssslide.SettingValues;

/**
 * Created by carlo_000 on 1/13/2016.
 */
public class ShouldOpenExternally {
    public static boolean contains(String target, String[] strings, boolean totalMatch) {
        for (String s : strings) {
            s = s.toLowerCase().trim();
            if (!s.isEmpty() && !s.equals("\n") && totalMatch ? target.equals(s) : target.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a domain should be filtered or not: returns true if the target domain ends with the
     * comparison domain and if supplied, target path begins with the comparison path
     *
     * @param target  URL to check
     * @param strings The URLs to check against
     * @return If the target is covered by any strings
     * @throws MalformedURLException
     */
    public static boolean isDomain(String target, String[] strings) throws MalformedURLException {
        URL domain = new URL(target);
        for (String s : strings) {
            if (!s.contains("/")) {
                if (hostContains(domain.getHost(), s)) {
                    return true;
                } else {
                    continue;
                }
            }

            if (!s.contains("://")) {
                s = "http://" + s;
            }

            try {
                URL comparison = new URL(s.toLowerCase());

                if (hostContains(domain.getHost(), comparison.getHost())
                        && domain.getPath().startsWith(comparison.getPath())) {
                    return true;
                }
            } catch (MalformedURLException ignored) {
            }
        }
        return false;
    }

    /**
     * Checks if {@code host} is contains by any of the provided {@code bases}
     * <p/>
     * For example "www.youtube.com" contains "youtube.com" but not "notyoutube.com" or
     * "youtube.co.uk"
     *
     * @param host  A hostname from e.g. {@link URI#getHost()}
     * @param bases Any number of hostnames to compare against {@code host}
     * @return If {@code host} contains any of {@code bases}
     */
    public static boolean hostContains(String host, String... bases) {
        if (host == null || host.isEmpty()) return false;

        for (String base : bases) {
            if (base == null || base.isEmpty()) continue;

            final int index = host.lastIndexOf(base);
            if (index < 0 || index + base.length() != host.length()) continue;
            if (base.length() == host.length() || host.charAt(index - 1) == '.') return true;
        }

        return false;
    }

    public static boolean openExternal(String url) {
        if (externalDomain == null) {
            externalDomain = SettingValues.alwaysExternal.replaceAll("^[,\\s]+", "").split("[,\\s]+");
        }
        try {
            return !SettingValues.alwaysExternal.isEmpty() && isDomain(url.toLowerCase(), externalDomain);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String[] externalDomain = null;
}
