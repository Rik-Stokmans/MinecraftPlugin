package aminecraftplugin.aminecraftplugin.drilling.drill;

import aminecraftplugin.aminecraftplugin.drilling.loot.LootFinder;
import aminecraftplugin.aminecraftplugin.drilling.resource.Resource;
import aminecraftplugin.aminecraftplugin.drilling.resource.resourceCategory;
import aminecraftplugin.aminecraftplugin.player.PlayerProfile;
import aminecraftplugin.aminecraftplugin.sideSkills.Skill;
import aminecraftplugin.aminecraftplugin.sideSkills.SkillType;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.line.HologramLine;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.structure.Structure;

import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static aminecraftplugin.aminecraftplugin.Main.*;
import static aminecraftplugin.aminecraftplugin.drilling.resource.ResourceSorters.*;
import static aminecraftplugin.aminecraftplugin.drilling.resource.Resource.categories;
import static aminecraftplugin.aminecraftplugin.drilling.resource.Resource.getResourceFromKey;
import static aminecraftplugin.aminecraftplugin.drilling.drill.DrillType.getDrillTypeFromName;
import static aminecraftplugin.aminecraftplugin.player.PlayerProfile.getPlayerProfile;
import static aminecraftplugin.aminecraftplugin.utils.ChatUtils.format;
import static aminecraftplugin.aminecraftplugin.utils.Compress.returnCompressed;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getCardinalDirection;
import static aminecraftplugin.aminecraftplugin.utils.Direction.getXandZ;
import static aminecraftplugin.aminecraftplugin.utils.IntegerToRoman.integerToRoman;
import static aminecraftplugin.aminecraftplugin.utils.RemoveHandItem.removeHandItem;
import static aminecraftplugin.aminecraftplugin.utils.defaultPageInventory.getDefaultScrollableInventory;
import static aminecraftplugin.aminecraftplugin.utils.log.logBase;

public class Drill implements aminecraftplugin.aminecraftplugin.drilling.structures.Structure {

    public static HashMap<Player, Drill> openedDrillInventory = new HashMap<>();
    private final static String[] veinTierList = {"Minimal", "Tiny", "Very Poor", "Poor", "Ample", "Small", "Modest", "Average", "Medium", "Considerable",
    "Sizable", "Large", "Abundant", "Great", "Huge", "Extremely Large", "Substantial", "Significant", "Plentiful", "Massive", "Vast", "Enormous",
    "Rich", "Gigantic", "Mammoth"};


    private OfflinePlayer owner;
    private Location location;
    private LootFinder lootFinder;
    private DrillType drillType;
    private ArrayList<Inventory> pages = new ArrayList<>();
    private int speedTier;
    private int depthTier;
    private Hologram hologram;
    private Structure structure;
    private HashMap<Integer, Double> resources = new HashMap<>();
    private ArrayList<BlockState> destroyedBlocks = new ArrayList<>();
    private int packetKey;
    private boolean muted;
    private ArrayList<Integer> tasks = new ArrayList<>();
    private double hp;
    private double energyReceiving;


    public static ItemStack getDrill(int speedTier, int depthTier, DrillType drillType){
        ItemStack drill = new ItemStack(Material.HOPPER);
        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(drill);
        NBTTagCompound nbt = nmsItem.u();
        if (nbt == null) nbt = new NBTTagCompound();
        nbt.a("drilldepth", depthTier);
        nbt.a("drillspeed", speedTier);
        nbt.a("drilltype", drillType.getNameFromDrillType());
        nmsItem.c(nbt);
        drill = CraftItemStack.asBukkitCopy(nmsItem);
        ItemMeta drillMeta = drill.getItemMeta();
        drillMeta.setDisplayName(format("&7" + drillType.getDisplayName()));
        ArrayList<String> lore = new ArrayList<>();
        lore.add(format("&7Speed&f: " + integerToRoman(speedTier)));
        lore.add(format("&7Depth&f: " + integerToRoman(depthTier)));
        lore.add(format("&7Stat multi&f: " + returnCompressed(drillType.getStatMulti(), 2)));
        drillMeta.setLore(lore);
        drill.setItemMeta(drillMeta);
        return drill;
    }

    public Drill(){
    }

    public Drill(Location location, OfflinePlayer owner, ItemStack drill){

        this.muted = false;
        this.location = location;
        this.owner = owner;

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(drill);
        NBTTagCompound nbt = nmsItem.u();
        String drillType = nbt.l("drilltype");
        int drillDepthTier = nbt.h("drilldepth");
        int drillSpeedTier = nbt.h("drillspeed");

        this.drillType = getDrillTypeFromName(drillType);
        this.depthTier = drillDepthTier;
        this.speedTier = drillSpeedTier;
        this.hp = this.getDrillType().getMaxHP();

        aminecraftplugin.aminecraftplugin.drilling.structures.Structure.addStructure(owner.getUniqueId(), this);

        //loot
        this.structure = aminecraftplugin.aminecraftplugin.drilling.structures.Structure.getStructure(this.getDrillType().getNameFromDrillType());
        this.hologram = initHologram();
        this.correctHologramPosition();
        this.lootFinder = new LootFinder(this.getLocation());
        this.initStructureInventories();
    }

    public Hologram initHologram(){
        Hologram hologram = api.createHologram(this.getLocation().clone().add(0.5, this.getThisStructure().getSize().getY() + 0.5, 0.5));
        hologram.getLines().appendText(format("&b" + this.getDrillType().getDisplayName()));
        hologram.getLines().appendText(this.getHealthBar());
        hologram.getLines().appendText(format("&eEnergy&7: " + "&e10 J&7/&eS"));
        hologram.getLines().appendText(format("&7&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-"));
        return hologram;
    }

    public double getDrillSpeed(){
        double baseSpeed = 0.005;
        baseSpeed *= Math.pow(Math.log((Math.E - 1) + this.getSpeedTier()), 0.5);
        baseSpeed *= this.getDrillType().getStatMulti();
        return baseSpeed;
    }

    public double getDepthFormulaNumber(){
        double baseDepth = this.getDepthTier();
        baseDepth *= this.getDrillType().getStatMulti();
        return baseDepth;
    }

    public long getDepth(){
        double depthTier = getDepthFormulaNumber();
        return 500 + (long) Math.ceil(depthTier * 50);
    }

    public String getHealthBar(){
        String healthBar = format("&cHealth&7: &7[&a");
        double percentage = this.getHP() / this.getMaxHP();
        int amountOfBars = (int) Math.ceil(percentage * 25);
        for (int i = 0; i < amountOfBars; i++){
            healthBar += "|";
        }
        healthBar += format("&7");
        for (int i = 25; i > amountOfBars; i--){
            healthBar += "|";
        }
        healthBar += format("&7]");
        return healthBar;
    }


    private void clearHologram(int keepLines){
        for (int i = this.getHologram().getLines().size(); i > keepLines; i--) {
            this.getHologram().getLines().remove(i - 1);
        }
    }
    private void correctHologramPosition(){
        this.getHologram().setPosition(this.getLocation().clone().add(0.5, this.getThisStructure().getSize().getY() + 0.2 + this.getHologram().getLines().getHeight(), 0.5));
    }

    private void scheduleLootFinding(OfflinePlayer p){

        int totalDuration = 25;
        double oneStoneDuration = (totalDuration / (300 * getDrillSpeed()));

        this.clearHologram(5);
        if (this.getHologram().getLines().size() == 5){
            TextHologramLine textHologramLine = (TextHologramLine) this.getHologram().getLines().get(4);
            textHologramLine.setText(format("&aSearching for resources"));
        } else {
            this.getHologram().getLines().appendText(format("&aSearching for resources"));
        }
        this.getHologram().getLines().appendText(format("&7&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-"));
        this.correctHologramPosition();
        LootFinder lootFinder = this.getLootFinder();
        Drill drill = this;

        if (!drill.isMuted()) {
            this.getLocation().getWorld().playSound(this.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
        }

        //set to stone
        this.getLocation().clone().add(0,-1,0).getBlock().setType(Material.STONE);

        final int[] stage = {0};
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packetContainer.getBlockPositionModifier().write(0, new BlockPosition((int) this.getLocation().getX(), (int) (this.getLocation().getY() - 1), (int) this.getLocation().getZ()));
        this.setPacketKey(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
        packetContainer.getIntegers().write(0, this.getPacketKey());

        //particles
        final ArrayList<BukkitTask>[] bukkitTasks = new ArrayList[]{new ArrayList<>()};
        BlockData stone = Material.STONE.createBlockData();

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {

                //particles
                BukkitTask task1 = new BukkitRunnable(){
                    @Override
                    public void run() {
                        double randomX = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                        while (randomX > -0.05 && randomX < 0.05) {
                            randomX = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                        }
                        double randomZ = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                        while (randomZ > -0.05 && randomZ < 0.05) {
                            randomZ = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                        }
                        drill.getLocation().getWorld().spawnParticle(Particle.BLOCK_CRACK, drill.getLocation().clone().add(0.5 + randomX, 0.2, 0.5 + randomZ), 1, stone);
                    }
                }.runTaskTimer(plugin, 0l, (long) Math.ceil((oneStoneDuration / 11) * 20)  / 3);
                bukkitTasks[0].add(task1);
                drill.getTasks().add(task1.getTaskId());

                //sound
                if (!drill.isMuted()) {
                    drill.getLocation().getWorld().playSound(drill.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
                }

                //hologram
                HologramLine hologramLine = drill.getHologram().getLines().get(4);
                TextHologramLine textHologramLine = (TextHologramLine) hologramLine;
                String currentLine = textHologramLine.getText();
                int dotCount = currentLine.length() - currentLine.replaceAll("\\.","").length();
                int newDotCount = dotCount + 1;
                if (newDotCount == 4) newDotCount = 0;
                String newLine = format("&aSearching for resources");
                for (int i = 0; i < newDotCount; i++){
                    newLine += ".";
                }
                textHologramLine.setText(newLine);

                //block stages
                if (stage[0] == 10){
                    packetContainer.getIntegers().write(1, -1);
                    protocolManager.broadcastServerPacket(packetContainer);

                    for (BukkitTask bukkitTask : bukkitTasks[0]){
                        Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
                    }
                    bukkitTasks[0] = new ArrayList<>();

                    PlayerProfile playerProfile = getPlayerProfile(p.getUniqueId());
                    Skill prospectingSkill = playerProfile.getSkills().get(SkillType.prospectingskill);
                    double treasureFindingChance = 25 * prospectingSkill.getMultiplier();

                    //debug
                    for (Player p : Bukkit.getOnlinePlayers()){
                        p.sendMessage(String.valueOf(prospectingSkill.getMultiplier()));
                    }
                    if (ThreadLocalRandom.current().nextDouble(100.0) <= treasureFindingChance) {
                        int veinTier = 1;
                        Skill miningSkill = playerProfile.getSkills().get(SkillType.miningskill);
                        double currentChance = 0.2 * Math.log(veinTier) * logBase(miningSkill.getTier() + 19, 20) + (0.2);
                        while (true){
                            double random = ThreadLocalRandom.current().nextDouble(0, 1);
                            if (random <= currentChance){
                                veinTier++;
                                currentChance = 0.2 * Math.log(veinTier) * logBase(miningSkill.getTier() + 19, 20) + (0.2);
                            } else {
                                break;
                            }
                            if (veinTier >= 25){
                                break;
                            }
                        }
                        int safeIndex = 0;
                        HashMap<Resource, Double> foundResources = lootFinder.findLoot(veinTier, p, drill.getDepth());
                        while (foundResources.isEmpty()) {
                            foundResources = lootFinder.findLoot(veinTier, p, drill.getDepth());
                            safeIndex++;
                            if (safeIndex > 5000) {
                                System.out.println("ERROR NO LOOT FOUND IN 5000 TRIES");
                                break;
                            }
                        }
                        if (foundResources.isEmpty()) {
                            scheduleLootFinding(p);
                        } else {
                            drill.scheduleLootMining(veinTier, foundResources, p);
                        }
                        drill.getTasks().remove(Integer.valueOf(this.getTaskId()));
                        this.cancel();
                    } else {
                        scheduleLootFinding(p);
                        this.cancel();
                    }
                }
                packetContainer.getIntegers().write(1, stage[0]);
                protocolManager.broadcastServerPacket(packetContainer);
                stage[0]++;
            }
        }.runTaskTimer(plugin, (long) Math.ceil((oneStoneDuration / 11) * 20), (long) Math.ceil((oneStoneDuration / 11) * 20));

        this.getTasks().add(task.getTaskId());
    }

    private void scheduleLootMining(int veinTier, HashMap<Resource, Double> resources, OfflinePlayer p){
        Drill drill = this;

        Double miningPerSecond = drill.getDrillSpeed();

        HashMap<Resource, Double> mined = new HashMap<>();
        for (Resource resource : resources.keySet()){
            mined.put(resource, 0.0);
        }

        this.clearHologram(4);
        this.getHologram().getLines().appendText(format("&7| &c" + veinTierList[veinTier - 1] + " &7(&e" + integerToRoman(veinTier) + "&7)" + " &7|"));
        for (Map.Entry<Resource, Double> entry : mined.entrySet()){
            Resource resource = entry.getKey();
            Double kgMined = entry.getValue();
            this.getHologram().getLines().appendText(format(resource.getName() + " &f" + returnCompressed(kgMined, 2, RoundingMode.HALF_UP) + "&7/&f" + returnCompressed(resources.get(resource), 2, RoundingMode.HALF_DOWN) + " Kg"));
        }
        this.getHologram().getLines().appendText(format("&7&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-&m-"));
        this.correctHologramPosition();


        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packetContainer.getBlockPositionModifier().write(0, new BlockPosition((int) this.getLocation().getX(), (int) (this.getLocation().getY() - 1), (int) this.getLocation().getZ()));
        packetContainer.getIntegers().write(0, this.getPacketKey());
        long totalDelay = 0;
        int totalSize = resources.keySet().size();
        final int[] countingSize = {0};
        for (Map.Entry<Resource, Double> entry : resources.entrySet()){
            Resource resource = entry.getKey();
            final double[] kgLeft = {entry.getValue()};

            this.getLocation().clone().add(0,-1,0).getBlock().setType(resource.getBlock());

            double totalSeconds = kgLeft[0] / miningPerSecond;
            final int[] stage = {0};


            //particles
            final ArrayList<BukkitTask>[] bukkitTasks = new ArrayList[]{new ArrayList<>()};
            BlockData blockType = resource.getBlock().createBlockData();
            BukkitTask task = new BukkitRunnable() {
                @Override
                public void run() {

                    BukkitTask task1 = new BukkitRunnable(){
                        @Override
                        public void run() {
                            double randomX = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                            while (randomX > -0.05 && randomX < 0.05) {
                                randomX = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                            }
                            double randomZ = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                            while (randomZ > -0.05 && randomZ < 0.05) {
                                randomZ = ThreadLocalRandom.current().nextDouble(-0.3, 0.3);
                            }
                            drill.getLocation().getWorld().spawnParticle(Particle.BLOCK_CRACK, drill.getLocation().clone().add(0.5 + randomX, 0.2, 0.5 + randomZ), 1, blockType);
                        }
                    }.runTaskTimer(plugin, 0l, (long) (((totalSeconds / 11)) * 20) / 3);
                    bukkitTasks[0].add(task1);
                    drill.getTasks().add(task1.getTaskId());

                    if (!drill.isMuted()) {
                        drill.getLocation().getWorld().playSound(drill.getLocation(), Sound.BLOCK_STONE_BREAK, 1, 1);
                    }
                    double kgMined = miningPerSecond * (totalSeconds / 11);
                    kgLeft[0] -= kgMined;
                    if (kgMined > kgLeft[0]) {
                        kgMined = kgLeft[0];
                    }
                    mined.replace(resource, mined.get(resource) + kgMined);

                    int j = drill.getHologram().getLines().size();
                    for (Map.Entry<Resource, Double> entry2 : mined.entrySet()) {
                        Resource resource2 = entry2.getKey();
                        Double kgMined2 = entry2.getValue();
                        for (int i = 0; i < j; i++) {
                            HologramLine hologramLine = drill.getHologram().getLines().get(i);
                            if (hologramLine instanceof TextHologramLine) {
                                TextHologramLine textHologramLine = (TextHologramLine) hologramLine;
                                if (textHologramLine.getText().contains(resource2.getName())) {
                                    textHologramLine.setText(format(resource2.getName() + " &f" + returnCompressed(kgMined2, 2, RoundingMode.HALF_UP) + "&7/&f" + returnCompressed(resources.get(resource2), 2, RoundingMode.HALF_DOWN) + " Kg"));
                                }
                            }
                        }
                    }
                    drill.addResource(resource.getKey(), kgMined);
                    if (stage[0] == 10) {
                        for (BukkitTask bukkitTask : bukkitTasks[0]){
                            Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
                        }
                        bukkitTasks[0] = new ArrayList<>();
                        packetContainer.getIntegers().write(1, -1);
                        protocolManager.broadcastServerPacket(packetContainer);
                        drill.getTasks().remove(Integer.valueOf(this.getTaskId()));
                        countingSize[0]++;
                        if (countingSize[0] >= totalSize) {
                            drill.scheduleLootFinding(p);
                        }
                        this.cancel();
                    }
                    packetContainer.getIntegers().write(1, stage[0]);
                    protocolManager.broadcastServerPacket(packetContainer);
                    stage[0]++;
                }
            }.runTaskTimer(plugin, (long) ((totalDelay + (totalSeconds / 11)) * 20), (long) (((totalSeconds / 11)) * 20));
            this.getTasks().add(task.getTaskId());

            totalDelay += totalSeconds + 0.05;
        }

    }

    @Override
    public void place(Player p){

        Location drillLoc = this.getLocation();

        //facing direction
        String s = getCardinalDirection(p);
        ImmutablePair<Integer, Integer> pair = getXandZ(s);

        //get structure
        int length = (int) this.getThisStructure().getSize().getZ();
        int width = (int) this.getThisStructure().getSize().getX();

        //get all blocks to be destroyed
        ArrayList<Location> locations = aminecraftplugin.aminecraftplugin.drilling.structures.Structure.getStructureSpace(drillLoc, this.getThisStructure(), pair);


        for (Location loc : locations){
            Block block = loc.getBlock();
            destroyedBlocks.add(block.getState());
        }
        for (Location loc : locations){
            Block block = loc.getBlock();
            block.setType(Material.AIR);
        }


        //placing structure with correct rotation
        StructureRotation structureRotation = StructureRotation.NONE;
        if (pair.getValue() == -1){
            structureRotation = StructureRotation.NONE;
        } else if (pair.getValue() == 1){
            structureRotation = StructureRotation.CLOCKWISE_180;
        } else if (pair.getKey() == 1){
            structureRotation = StructureRotation.CLOCKWISE_90;
        } else if (pair.getKey() == -1){
            structureRotation = StructureRotation.COUNTERCLOCKWISE_90;
        }

        structure.place(this.getLocation().clone().add(
                        pair.getValue() * Math.ceil(width / 2) + pair.getKey() * Math.ceil(length / 2),
                        0,
                        -pair.getKey() * Math.ceil(width / 2) + pair.getValue() * Math.floor(length / 2)),
                true, structureRotation, Mirror.NONE, 0, 1, new Random());

        this.scheduleLootFinding(this.getOwner());
    }



    @Override
    public ItemStack destroy(UUID uuid, boolean offline){
        this.collectResources(uuid, new ArrayList<>(this.getResources().keySet()), true);
        this.getHologram().delete();
        for (Integer taskID : this.getTasks()){
            Bukkit.getScheduler().cancelTask(taskID);
        }
        for (int i = 0; i < 2; i++) {
            for (BlockState destroyedBlock : destroyedBlocks) {
                destroyedBlock.update(true);
                if (destroyedBlock.getBlockData().getAsString().contains("half=")
                        && !destroyedBlock.getType().toString().contains("STAIRS")
                        && !destroyedBlock.getType().toString().contains("TRAPDOOR")) {
                    if (!offline) {
                        if (destroyedBlock.getBlockData().getAsString().contains("half=upper")) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    destroyedBlock.getBlock().setType(destroyedBlock.getType());
                                    Location locDown = destroyedBlock.getLocation().clone().add(0, -1, 0);
                                    BlockData blockData2 = Bukkit.createBlockData(destroyedBlock.getType(), "[half=lower]");
                                    locDown.getBlock().setBlockData(blockData2);
                                }
                            }.runTaskLater(plugin, 2l);
                        } else if (destroyedBlock.getBlockData().getAsString().contains("half=lower")) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    destroyedBlock.getBlock().setType(destroyedBlock.getType());
                                    Location locUp = destroyedBlock.getLocation().clone().add(0, 1, 0);
                                    BlockData blockData2 = Bukkit.createBlockData(destroyedBlock.getType(), "[half=upper]");
                                    locUp.getBlock().setBlockData(blockData2);
                                }
                            }.runTaskLater(plugin, 2l);
                        }
                    } else {
                        if (!savedBlocks.containsKey(destroyedBlock.getLocation())) {
                            savedBlocks.put(destroyedBlock.getLocation(), destroyedBlock.getBlockData());
                        }
                    }

                }
            }
        }
        PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packetContainer.getBlockPositionModifier().write(0, new BlockPosition((int) this.getLocation().getX(), (int) (this.getLocation().getY() - 1), (int) this.getLocation().getZ()));
        packetContainer.getIntegers().write(0, this.getPacketKey());
        packetContainer.getIntegers().write(1, -1);
        protocolManager.broadcastServerPacket(packetContainer);

        structures.get(uuid).remove(this);

        return getDrill(this.getSpeedTier(), this.getDepthTier(), this.getDrillType());
    }


    public void placeEvent(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        e.setCancelled(true);
        if (!e.getBlockReplacedState().getType().equals(Material.AIR)) {
            p.sendMessage(format("&cYou can't place that here"));
            return;
        }
        ItemStack itemPlaced = e.getItemInHand();
        if (CraftItemStack.asNMSCopy(itemPlaced).u() == null) return;
        Location placedLoc = e.getBlock().getLocation();

        net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemPlaced);
        NBTTagCompound nbt = nmsItem.u();
        String drillType = nbt.l("drilltype");

        //check if near structures have enough distance
        if (!aminecraftplugin.aminecraftplugin.drilling.structures.Structure.canBePlaced(drillType, placedLoc, p))
            return;

        //remove drill from hand
        removeHandItem(p, e.getHand());

        //create drill and place
        Drill drill = new Drill(placedLoc, p, itemPlaced);
        new BukkitRunnable() {
            @Override
            public void run() {
                drill.place(p);
            }
        }.runTaskLater(plugin, 1l);
    }


    public void addResource(Integer key, Double kg){
        if (!this.getResources().containsKey(key)){
            this.getResources().put(key, kg);
        } else {
            this.getResources().put(key, this.getResources().get(key) + kg);
        }
        this.updateInventories();
    }

    public int getMaxAmountOfPages(resourceCategory filterCategory){
        List<Map.Entry<Integer, Double>> resourceList = this.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        int size = resourceList.size();
        int amountOfPages = (int) Math.ceil(size / 36);
        if (amountOfPages == 0) return 1;
        return amountOfPages;
    }

    public ItemStack getMuteButton(){
        ItemStack muteButton = new ItemStack(Material.JUKEBOX);
        ItemMeta metaMuteButton = muteButton.getItemMeta();
        if (this.isMuted()) {
            metaMuteButton.setDisplayName(format("&aClick to unmute"));
        } else {
            metaMuteButton.setDisplayName(format("&cClick to mute"));
        }
        muteButton.setItemMeta(metaMuteButton);
        return muteButton;
    }

    public ItemStack getSellAllButton(){
        ItemStack collectAllButton = new ItemStack(Material.CHEST);
        ItemMeta metaCollectAllButton = collectAllButton.getItemMeta();
        metaCollectAllButton.setDisplayName(format("&aClick to collect all"));
        collectAllButton.setItemMeta(metaCollectAllButton);
        return collectAllButton;
    }

    public ItemStack getPickupButton(){
        ItemStack pickupButton = new ItemStack(Material.BARRIER);
        ItemMeta metaPickupButton = pickupButton.getItemMeta();
        metaPickupButton.setDisplayName(format("&c&nClick to pick up drill"));
        pickupButton.setItemMeta(metaPickupButton);
        return pickupButton;
    }

    public Inventory createNewInventory(int page, int maxPages){
        PlayerProfile owner = getPlayerProfile(this.getOwner().getUniqueId());
        String inventoryName = this.getDrillType().getDisplayName() + " page " + page + "/" + maxPages;
        Inventory inventory = getDefaultScrollableInventory(inventoryName, false);
        inventory.setItem(51, getSortItem(owner.getSortingIndex()));
        inventory.setItem(52, getFilterItem(owner.getFilterCategory()));
        inventory.setItem(46, this.getMuteButton());
        inventory.setItem(49, this.getSellAllButton());
        inventory.setItem(47, this.getPickupButton());
        return inventory;
    }

    public void initStructureInventories(){
        PlayerProfile owner = getPlayerProfile(this.getOwner().getUniqueId());
        int maxAmountOfPages = getMaxAmountOfPages(owner.getFilterCategory());
        ArrayList<Inventory> inventories = new ArrayList<>();
        for (int i = 0; i < maxAmountOfPages; i++){
            inventories.add(createNewInventory(i + 1, maxAmountOfPages));
        }
        this.setPages(inventories);
    }

    public void updateMuteButton(){
        ArrayList<Inventory> inventories = this.getPages();
        for (Inventory inventory : inventories) {
            inventory.setItem(46, this.getMuteButton());
        }
    }

    public void updateInventories(){


        PlayerProfile owner = getPlayerProfile(this.getOwner().getUniqueId());
        int sortingIndex = owner.getSortingIndex();
        resourceCategory filterCategory = owner.getFilterCategory();

        ItemStack sorter = getSortItem(sortingIndex);
        ItemStack filter = getFilterItem(filterCategory);

        int maxAmountOfPages = getMaxAmountOfPages(owner.getFilterCategory());
        ArrayList<Inventory> inventories = this.getPages();
        int amountOfPages = inventories.size();

        for (int i = 0; i < maxAmountOfPages - amountOfPages; i++){
            inventories.add(createNewInventory(amountOfPages + i + 1, maxAmountOfPages));
        }
        for (int i = amountOfPages - maxAmountOfPages; i > 0; i--){
            inventories.remove(amountOfPages - i + 1);
        }

        List<Map.Entry<Integer, Double>> resourceList = this.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        Collections.sort(resourceList, resourceComparators[sortingIndex]);

        int inventoryIndex = 0;
        int slotIndex = 0;

        for (Inventory inventory : inventories){
            for (int i = 0; i < 36; i++){
                inventory.setItem(i, null);
            }
            inventory.setItem(51, sorter);
            inventory.setItem(52, filter);
        }

        for (Map.Entry<Integer, Double> entry : resourceList){
            if (slotIndex >= 36){
                slotIndex = 0;
                inventoryIndex++;
            }
            Resource resource = getResourceFromKey(entry.getKey());
            double weight = entry.getValue();
            ItemStack item = resource.getItemStack();
            ItemMeta itemMeta = item.getItemMeta();
            itemMeta.setDisplayName(format("&f" + itemMeta.getDisplayName()));
            ArrayList<String> lore = new ArrayList<>();
            lore.add(format("&7Weight: &f" + returnCompressed(weight, 2) + "kg"));
            lore.add("");
            lore.add(format("&a&nClick to collect"));
            itemMeta.setLore(lore);
            item.setItemMeta(itemMeta);
            inventories.get(inventoryIndex).setItem(slotIndex, item);
            slotIndex++;
        }

    }

    @Override
    public void openStructureMenu(Player p, int page) {
        Inventory inventory = this.getPages().get(page - 1);
        p.openInventory(inventory);
        openedDrillInventory.put(p, this);
    }

    public int getAmountOfResources(int sortingIndex, resourceCategory filterCategory){
        List<Map.Entry<Integer, Double>> resourceList = this.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        Collections.sort(resourceList, resourceComparators[sortingIndex]);
        return resourceList.size();
    }

    public void collectResources(UUID uuid, ArrayList<Integer> indexes, boolean silent){
        PlayerProfile playerProfile = getPlayerProfile(uuid);
        int sortingIndex = playerProfile.getSortingIndex();
        resourceCategory filterCategory = playerProfile.getFilterCategory();
        List<Map.Entry<Integer, Double>> resourceList = this.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
        Collections.sort(resourceList, resourceComparators[sortingIndex]);
        for (int index : indexes) {
            double kg = this.getResources().get(Integer.valueOf(index));
            int key = index;
            double leftOver = playerProfile.getBackPack().addResource(key, kg);
            this.getResources().remove(Integer.valueOf(key));
            if (leftOver > 0) {
                this.getResources().put(key, leftOver);
                break;
            }
            if (Bukkit.getOfflinePlayer(uuid).isOnline() && !silent){
                Player p = Bukkit.getPlayer(uuid);
                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }
        if (Bukkit.getOfflinePlayer(uuid).isOnline() && silent){
            Player p = Bukkit.getPlayer(uuid);
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
        this.updateInventories();
    }

    public static void drillInventoryClickEvent(InventoryClickEvent e){
        if (e.getView() == null) return;
        String name = e.getView().getTitle();
        if (name.contains("drill")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            PlayerProfile playerProfile = getPlayerProfile(p);
            Drill drill = openedDrillInventory.get(p);
            int sortingIndex = playerProfile.getSortingIndex();
            resourceCategory filterCategory = playerProfile.getFilterCategory();
            int maxAmountOfPages = drill.getMaxAmountOfPages(filterCategory);
            int currentPage = Integer.parseInt(name.split("page ")[1].split("/")[0]);
            if (e.getRawSlot() < 36 && e.getRawSlot() >= 0){
                List<Map.Entry<Integer, Double>> resourceList = drill.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
                Collections.sort(resourceList, resourceComparators[sortingIndex]);
                int index = e.getRawSlot() + (35 * (currentPage - 1));
                ArrayList<Integer> indexes = new ArrayList<>();
                indexes.add(resourceList.get(index).getKey());
                drill.collectResources(p.getUniqueId(), indexes, false);
            } else {
                switch (e.getRawSlot()) {
                    case 47:
                        p.closeInventory();
                        if (drill.hasMaxHP()) {
                            p.getInventory().addItem(drill.destroy(p.getUniqueId(), false));
                        } else {
                            p.sendMessage(format("&c&nCan not pick up while drill is not max health"));
                        }
                        break;
                    case 49:
                        List<Map.Entry<Integer, Double>> resourceList = drill.getResources().entrySet().stream().filter(entry -> categories.get(filterCategory).contains(entry.getKey())).collect(Collectors.toList());
                        Collections.sort(resourceList, resourceComparators[sortingIndex]);
                        int amountOfItems = drill.getAmountOfResources(sortingIndex, filterCategory);
                        ArrayList<Integer> indexes = new ArrayList<>();
                        for (int i = 0; i < amountOfItems; i++){
                            indexes.add(resourceList.get(i).getKey());
                        }
                        drill.collectResources(p.getUniqueId(), indexes, true);
                        break;
                    case 46:
                        if (drill.isMuted()) {
                            drill.setMuted(false);
                        } else {
                            drill.setMuted(true);
                        }
                        drill.updateMuteButton();
                        break;
                    case 45:
                        if (currentPage > 1) {
                            drill.openStructureMenu(p, currentPage - 1);
                        }
                        break;
                    case 53:
                        if (currentPage + 1 <= maxAmountOfPages) {
                            drill.openStructureMenu(p, currentPage + 1);
                        }
                        break;
                    case 51:
                        int sortingIndex1 = sortingIndex;
                        if (e.getClick().equals(ClickType.LEFT)) {
                            if (sortingIndex1 == resourceComparators.length - 1) {
                                sortingIndex1 = 0;
                            } else {
                                sortingIndex1++;
                            }
                        } else if (e.getClick().equals(ClickType.RIGHT)) {
                            if (sortingIndex1 == 0) {
                                sortingIndex1 = resourceComparators.length - 1;
                            } else {
                                sortingIndex1--;
                            }
                        }
                        playerProfile.setSortingIndex(sortingIndex1);
                        drill.updateInventories();
                        drill.openStructureMenu(p, 1);
                        break;
                    case 52:
                        int listIndex = 0;
                        int index = 0;
                        for (resourceCategory resourceCategory : resourceCategory.values()) {
                            if (filterCategory.equals(resourceCategory)) {
                                listIndex = index;
                            }
                            index++;
                        }
                        if (e.getClick().equals(ClickType.LEFT)) {
                            if (listIndex == resourceCategory.values().length - 1) {
                                listIndex = 0;
                            } else {
                                listIndex++;
                            }
                        } else if (e.getClick().equals(ClickType.RIGHT)) {
                            if (listIndex == 0) {
                                listIndex = resourceCategory.values().length - 1;
                            } else {
                                listIndex--;
                            }
                        }
                        playerProfile.setFilterCategory(resourceCategory.values()[listIndex]);
                        drill.updateInventories();
                        drill.openStructureMenu(p, 1);
                        break;
                }
            }
        }

    }

    @Override
    public String getStructureName() {
        return this.getDrillType().getNameFromDrillType();
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public ArrayList<Location> getLocations() {
        ArrayList<Location> locations = new ArrayList<>();
        for (BlockState blockState : this.getDestroyedBlocks()){
            locations.add(blockState.getLocation());
        }
        return locations;
    }


    public ArrayList<Inventory> getPages() {
        return this.pages;
    }

    public void setPages(ArrayList<Inventory> pages) {
        this.pages = pages;
    }

    public OfflinePlayer getOwner() {
        return owner;
    }

    public DrillType getDrillType() {
        return drillType;
    }

    public ArrayList<BlockState> getDestroyedBlocks() {
        return destroyedBlocks;
    }

    public LootFinder getLootFinder() {
        return lootFinder;
    }

    public HashMap<Integer, Double> getResources() {
        return resources;
    }

    public Hologram getHologram() {
        return hologram;
    }

    public Structure getThisStructure() {
        return structure;
    }

    public int getPacketKey() {
        return packetKey;
    }

    public void setPacketKey(int packetKey) {
        this.packetKey = packetKey;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public ArrayList<Integer> getTasks() {
        return tasks;
    }

    public double getMaxHP(){
        double maxHP = this.getDrillType().getMaxHP();
        return maxHP;
    }

    public double getHP(){
        return this.hp;
    }

    public boolean hasMaxHP(){
        if (this.getHP() == this.getHP()) return true;
        return false;
    }

    public int getSpeedTier() {
        return speedTier;
    }

    public int getDepthTier() {
        return this.depthTier;
    }
}
