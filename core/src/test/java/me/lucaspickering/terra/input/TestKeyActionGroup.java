package me.lucaspickering.terra.input;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestKeyActionGroup {

    @Test
    public void testGetActionByNameWorks() {
        assertEquals(KeyAction.WORLD_TILECOLOR_BIOME,
                     KeyActionGroup.getActionByName("world.tileColor.biome"));
        assertEquals(KeyAction.WORLD_TILEOVERLAY_RUNOFFLEVEL,
                     KeyActionGroup.getActionByName("world.tileOverlay.runoffLevel"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionByNameDotOnly() {
        KeyActionGroup.getActionByName("."); // Fail because it is just a dot
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionByNameDotPrefix() {
        // Fail because it starts with a dot
        KeyActionGroup.getActionByName(".world.tileColor.biome");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionByNameDotSuffix() {
        // Fail because it starts with a dot
        KeyActionGroup.getActionByName("world.tileColor.biome.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionByNameFakeGroup() {
        KeyActionGroup.getActionByName("fake.group"); // Fail because the group doesn't exist
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionByNameFakeSubgroup() {
        KeyActionGroup.getActionByName("world.group"); // Fail because the subgroup doesn't exist
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionByNameNoAction() {
        KeyActionGroup.getActionByName("world.tileColor"); // Fail because there's no action
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionByNameBogusGroup() {
        // Fail because it starts with a dot
        KeyActionGroup.getActionByName("world.fake.biome");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetActionByNameBogusSuffix() {
        // Fail because there's bogus after the action
        KeyActionGroup.getActionByName("world.tileColor.biome.fake");
    }
}
