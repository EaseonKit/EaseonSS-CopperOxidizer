package com.easeon.ss.copperoxidizer;

import com.easeon.ss.core.api.common.base.BaseToggleModule;
import com.easeon.ss.core.api.definitions.enums.EventPhase;
import com.easeon.ss.core.api.events.EaseonBlockUse;
import com.easeon.ss.core.api.events.EaseonBlockUse.BlockUseTask;
import com.easeon.ss.core.api.events.EaseonEntityInteract;
import com.easeon.ss.core.api.events.EaseonEntityInteract.EntityInteractTask;
import com.easeon.ss.core.helper.CopperHelper;
import net.fabricmc.api.ModInitializer;

public class Easeon extends BaseToggleModule implements ModInitializer {
    private BlockUseTask blockUseTask;
    private EntityInteractTask entityInteractTask;
    public static Easeon instance;

    public Easeon() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        CopperHelper.init();
        logger.info("Initialized!");
    }

    public void updateTask() {
        if (config.enabled && blockUseTask == null && entityInteractTask == null) {
            blockUseTask = EaseonBlockUse.on(EventPhase.BEFORE, EaseonBlockUseHandler::onUseBlock);
            entityInteractTask = EaseonEntityInteract.on(EventPhase.BEFORE, EaseonEntityInteractHandler::onUseEntity);
        }
        if (!config.enabled && blockUseTask != null && entityInteractTask != null) {
            EaseonBlockUse.unregister(blockUseTask);
            EaseonEntityInteract.unregister(entityInteractTask);
            blockUseTask = null;
            entityInteractTask = null;
        }
    }
}