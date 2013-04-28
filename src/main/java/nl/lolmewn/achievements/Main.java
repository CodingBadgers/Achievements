package nl.lolmewn.achievements;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import nl.lolmewn.stats.api.StatsAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import sun.reflect.ConstructorAccessor;
import sun.reflect.FieldAccessor;
import sun.reflect.ReflectionFactory;

public class Main extends JavaPlugin {
    
    private StatsAPI api;
    private Settings settings;
    private AchievementManager aManager;
    
    @Override
    public void onDisable() {
        
    }
    
    @Override
    public void onEnable() {
        Plugin stats = this.getServer().getPluginManager().getPlugin("Stats");
        if (stats == null) {
            this.getLogger().severe("Stats not found, disabling! You can download stats here: http://dev.bukkit.org/server-mods/lolmewnstats/");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        api = getServer().getServicesManager().getRegistration(nl.lolmewn.stats.api.StatsAPI.class).getProvider();
        settings = new Settings(this);
        settings.checkExistance();
        settings.loadConfig();
        aManager = new AchievementManager(this);
        aManager.loadAchievements();
        for (Achievement ach : aManager.getAchievements()) {
            this.addEnum(org.bukkit.Achievement.class, ach.getName());
        }
    }
    
    public Settings getSettings() {
        return settings;
    }
    
    public StatsAPI getAPI() {
        return api;
    }
    
    public AchievementManager getAchievementManager() {
        return aManager;
    }
    
    public void debug(String message) {
        if (this.getSettings().isDebug()) {
            this.getLogger().info("[Debug] " + message);
        }
    }
    private ReflectionFactory reflectionFactory = ReflectionFactory.getReflectionFactory();
    
    private void setFailsafeFieldValue(Field field, Object target, Object value) throws NoSuchFieldException,
            IllegalAccessException {
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        int modifiers = modifiersField.getInt(field);
        modifiers &= ~Modifier.FINAL;
        modifiersField.setInt(field, modifiers);
        FieldAccessor fa = reflectionFactory.newFieldAccessor(field, false);
        fa.set(target, value);
    }
    
    private void blankField(Class<?> enumClass, String fieldName) throws NoSuchFieldException,
            IllegalAccessException {
        for (Field field : Class.class.getDeclaredFields()) {
            if (field.getName().contains(fieldName)) {
                AccessibleObject.setAccessible(new Field[]{field}, true);
                setFailsafeFieldValue(field, enumClass, null);
                break;
            }
        }
    }
    
    private void cleanEnumCache(Class<?> enumClass) throws NoSuchFieldException, IllegalAccessException {
        blankField(enumClass, "enumConstantDirectory"); 
        blankField(enumClass, "enumConstants"); 
    }
    
    private ConstructorAccessor getConstructorAccessor(Class<?> enumClass, Class<?>[] additionalParameterTypes)
            throws NoSuchMethodException {
        Class<?>[] parameterTypes = new Class[additionalParameterTypes.length + 2];
        parameterTypes[0] = String.class;
        parameterTypes[1] = int.class;
        System.arraycopy(additionalParameterTypes, 0, parameterTypes, 2, additionalParameterTypes.length);
        return reflectionFactory.newConstructorAccessor(enumClass.getDeclaredConstructor(parameterTypes));
    }
    
    private Object makeEnum(Class<?> enumClass, String value, int ordinal, Class<?>[] additionalTypes,
            Object[] additionalValues) throws Exception {
        Object[] parms = new Object[additionalValues.length + 2];
        parms[0] = value;
        parms[1] = Integer.valueOf(ordinal);
        System.arraycopy(additionalValues, 0, parms, 2, additionalValues.length);
        return enumClass.cast(getConstructorAccessor(enumClass, additionalTypes).newInstance(parms));
    }

    /**
     * Add an enum instance to the enum class given as argument
     *
     * @param <T> the type of the enum (implicit)
     * @param enumType the class of the enum to be modified
     * @param enumName the name of the new enum instance to be added to the
     * class.
     */
    public <T extends Enum<?>> void addEnum(Class<T> enumType, String enumName) {
        if (!Enum.class.isAssignableFrom(enumType)) {
            throw new RuntimeException("class " + enumType + " is not an instance of Enum");
        }
        Field valuesField = null;
        Field[] fields = org.bukkit.Achievement.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().contains("$VALUES")) {
                valuesField = field;
                break;
            }
        }
        AccessibleObject.setAccessible(new Field[]{valuesField}, true);
        try {
            T[] previousValues = (T[]) valuesField.get(enumType);
            List<T> values = new ArrayList<T>(Arrays.asList(previousValues));
            T newValue = (T) makeEnum(enumType, 
                    enumName, 
                    values.size(),
                    new Class<?>[]{int.class}, 
                    new Object[]{org.bukkit.Achievement.values().length + 1});
            values.add(newValue);
            setFailsafeFieldValue(valuesField, null, values.toArray((T[]) Array.newInstance(enumType, 0)));
            cleanEnumCache(enumType);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cm, String[] args){
        if(args.length == 1 && args[0].equalsIgnoreCase("test")){
            org.bukkit.Achievement[] array = org.bukkit.Achievement.values();
            for(org.bukkit.Achievement a : array){
                System.out.println(a.toString() + " with ID " + a.getId());
            }
            return true;
        }
        return false;
    }
}
