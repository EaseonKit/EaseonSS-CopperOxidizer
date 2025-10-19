package com.easeon.ss.copperoxidizer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Easeon implements ModInitializer {
    public static final String MOD_ID = "easeon-copper-oxidizer";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ConfigManager CONFIG = new ConfigManager();

    @Override
    public void onInitialize() {
        LOGGER.info("Copper Oxidizer Mod Initializing...");

        // Config 로드
        CONFIG.load();

        // 명령어 등록
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EaseonCommand.register(dispatcher);
            LOGGER.info("Commands registered!");
        });

        // 블록 우클릭 이벤트 등록
//        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            UseBlockCallback.EVENT.register(OxidationHandler::onUseBlock);
            UseEntityCallback.EVENT.register(CopperGolemOxidationHandler::onUseEntity);
//        }

        LOGGER.info("Copper Oxidizer Mod Initialized!");
    }
}