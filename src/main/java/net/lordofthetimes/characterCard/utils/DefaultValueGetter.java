package net.lordofthetimes.characterCard.utils;

import dev.dejvokep.boostedyaml.YamlDocument;

import java.util.List;
import java.util.Random;

public class DefaultValueGetter {
    static public String getDefaultName(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("name.default");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultAge(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("age.default");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultRace(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("race.default");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultGender(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("gender.default");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultReligion(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("religion.default");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultDescription(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("description.default");

        return values.get(random.nextInt(values.size()));
    }

    static public String getDefaultLore(YamlDocument config){
        Random random = new Random();
        List<String> values = config.getStringList("lore.default");

        return values.get(random.nextInt(values.size()));
    }
}
