package net.lordofthetimes.characterCard.utils;

import dev.dejvokep.boostedyaml.YamlDocument;

import java.util.List;
import java.util.Random;

public class DefaultValueGetter {
    static public String getDefaultName(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("defaultName");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultAge(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("defaultAge");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultRace(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("defaultRace");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultGender(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("defaultGender");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultReligion(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("defaultReligion");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultDescription(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("defaultDescription");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultLore(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("defaultLore");

        return values.get(random.nextInt(values.size()));
    }
}
