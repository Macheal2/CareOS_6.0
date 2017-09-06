/*
 * Copyright (C) 2011-2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: /home/zhouhua/zhouhua/C687V02-20170601-RNG/cappu37_ALPS-MP-M0.MP1-V2.164.3_MAGC6737M_65_C_M0/alps/packages/apps/DeskClock/src/com/android/deskclock/widget/fall/fall.rs
 */
package com.android.deskclock.widget.fall;

import android.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_fall extends ScriptC {
    private static final String __rs_resource_name = "fall";
    // Constructor
    public  ScriptC_fall(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_fall(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __F32 = Element.F32(rs);
        __PROGRAM_VERTEX = Element.PROGRAM_VERTEX(rs);
        __PROGRAM_FRAGMENT = Element.PROGRAM_FRAGMENT(rs);
        __PROGRAM_STORE = Element.PROGRAM_STORE(rs);
        __ALLOCATION = Element.ALLOCATION(rs);
        __MESH = Element.MESH(rs);
    }

    private Element __ALLOCATION;
    private Element __F32;
    private Element __MESH;
    private Element __PROGRAM_FRAGMENT;
    private Element __PROGRAM_STORE;
    private Element __PROGRAM_VERTEX;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_F32;
    private FieldPacker __rs_fp_MESH;
    private FieldPacker __rs_fp_PROGRAM_FRAGMENT;
    private FieldPacker __rs_fp_PROGRAM_STORE;
    private FieldPacker __rs_fp_PROGRAM_VERTEX;
    private final static int mExportVarIdx_g_glWidth = 0;
    private float mExportVar_g_glWidth;
    public synchronized void set_g_glWidth(float v) {
        setVar(mExportVarIdx_g_glWidth, v);
        mExportVar_g_glWidth = v;
    }

    public float get_g_glWidth() {
        return mExportVar_g_glWidth;
    }

    public Script.FieldID getFieldID_g_glWidth() {
        return createFieldID(mExportVarIdx_g_glWidth, null);
    }

    private final static int mExportVarIdx_g_glHeight = 1;
    private float mExportVar_g_glHeight;
    public synchronized void set_g_glHeight(float v) {
        setVar(mExportVarIdx_g_glHeight, v);
        mExportVar_g_glHeight = v;
    }

    public float get_g_glHeight() {
        return mExportVar_g_glHeight;
    }

    public Script.FieldID getFieldID_g_glHeight() {
        return createFieldID(mExportVarIdx_g_glHeight, null);
    }

    private final static int mExportVarIdx_g_meshWidth = 2;
    private float mExportVar_g_meshWidth;
    public synchronized void set_g_meshWidth(float v) {
        setVar(mExportVarIdx_g_meshWidth, v);
        mExportVar_g_meshWidth = v;
    }

    public float get_g_meshWidth() {
        return mExportVar_g_meshWidth;
    }

    public Script.FieldID getFieldID_g_meshWidth() {
        return createFieldID(mExportVarIdx_g_meshWidth, null);
    }

    private final static int mExportVarIdx_g_meshHeight = 3;
    private float mExportVar_g_meshHeight;
    public synchronized void set_g_meshHeight(float v) {
        setVar(mExportVarIdx_g_meshHeight, v);
        mExportVar_g_meshHeight = v;
    }

    public float get_g_meshHeight() {
        return mExportVar_g_meshHeight;
    }

    public Script.FieldID getFieldID_g_meshHeight() {
        return createFieldID(mExportVarIdx_g_meshHeight, null);
    }

    private final static int mExportVarIdx_g_xOffset = 4;
    private float mExportVar_g_xOffset;
    public synchronized void set_g_xOffset(float v) {
        setVar(mExportVarIdx_g_xOffset, v);
        mExportVar_g_xOffset = v;
    }

    public float get_g_xOffset() {
        return mExportVar_g_xOffset;
    }

    public Script.FieldID getFieldID_g_xOffset() {
        return createFieldID(mExportVarIdx_g_xOffset, null);
    }

    private final static int mExportVarIdx_g_rotate = 5;
    private float mExportVar_g_rotate;
    public synchronized void set_g_rotate(float v) {
        setVar(mExportVarIdx_g_rotate, v);
        mExportVar_g_rotate = v;
    }

    public float get_g_rotate() {
        return mExportVar_g_rotate;
    }

    public Script.FieldID getFieldID_g_rotate() {
        return createFieldID(mExportVarIdx_g_rotate, null);
    }

    private final static int mExportVarIdx_g_PVWater = 6;
    private ProgramVertex mExportVar_g_PVWater;
    public synchronized void set_g_PVWater(ProgramVertex v) {
        setVar(mExportVarIdx_g_PVWater, v);
        mExportVar_g_PVWater = v;
    }

    public ProgramVertex get_g_PVWater() {
        return mExportVar_g_PVWater;
    }

    public Script.FieldID getFieldID_g_PVWater() {
        return createFieldID(mExportVarIdx_g_PVWater, null);
    }

    private final static int mExportVarIdx_g_PVSky = 7;
    private ProgramVertex mExportVar_g_PVSky;
    public synchronized void set_g_PVSky(ProgramVertex v) {
        setVar(mExportVarIdx_g_PVSky, v);
        mExportVar_g_PVSky = v;
    }

    public ProgramVertex get_g_PVSky() {
        return mExportVar_g_PVSky;
    }

    public Script.FieldID getFieldID_g_PVSky() {
        return createFieldID(mExportVarIdx_g_PVSky, null);
    }

    private final static int mExportVarIdx_g_PFSky = 8;
    private ProgramFragment mExportVar_g_PFSky;
    public synchronized void set_g_PFSky(ProgramFragment v) {
        setVar(mExportVarIdx_g_PFSky, v);
        mExportVar_g_PFSky = v;
    }

    public ProgramFragment get_g_PFSky() {
        return mExportVar_g_PFSky;
    }

    public Script.FieldID getFieldID_g_PFSky() {
        return createFieldID(mExportVarIdx_g_PFSky, null);
    }

    private final static int mExportVarIdx_g_PFSLeaf = 9;
    private ProgramStore mExportVar_g_PFSLeaf;
    public synchronized void set_g_PFSLeaf(ProgramStore v) {
        setVar(mExportVarIdx_g_PFSLeaf, v);
        mExportVar_g_PFSLeaf = v;
    }

    public ProgramStore get_g_PFSLeaf() {
        return mExportVar_g_PFSLeaf;
    }

    public Script.FieldID getFieldID_g_PFSLeaf() {
        return createFieldID(mExportVarIdx_g_PFSLeaf, null);
    }

    private final static int mExportVarIdx_g_PFBackground = 10;
    private ProgramFragment mExportVar_g_PFBackground;
    public synchronized void set_g_PFBackground(ProgramFragment v) {
        setVar(mExportVarIdx_g_PFBackground, v);
        mExportVar_g_PFBackground = v;
    }

    public ProgramFragment get_g_PFBackground() {
        return mExportVar_g_PFBackground;
    }

    public Script.FieldID getFieldID_g_PFBackground() {
        return createFieldID(mExportVarIdx_g_PFBackground, null);
    }

    private final static int mExportVarIdx_g_TLeaves = 11;
    private Allocation mExportVar_g_TLeaves;
    public synchronized void set_g_TLeaves(Allocation v) {
        setVar(mExportVarIdx_g_TLeaves, v);
        mExportVar_g_TLeaves = v;
    }

    public Allocation get_g_TLeaves() {
        return mExportVar_g_TLeaves;
    }

    public Script.FieldID getFieldID_g_TLeaves() {
        return createFieldID(mExportVarIdx_g_TLeaves, null);
    }

    private final static int mExportVarIdx_g_TRiverbed = 12;
    private Allocation mExportVar_g_TRiverbed;
    public synchronized void set_g_TRiverbed(Allocation v) {
        setVar(mExportVarIdx_g_TRiverbed, v);
        mExportVar_g_TRiverbed = v;
    }

    public Allocation get_g_TRiverbed() {
        return mExportVar_g_TRiverbed;
    }

    public Script.FieldID getFieldID_g_TRiverbed() {
        return createFieldID(mExportVarIdx_g_TRiverbed, null);
    }

    private final static int mExportVarIdx_g_WaterMesh = 13;
    private Mesh mExportVar_g_WaterMesh;
    public synchronized void set_g_WaterMesh(Mesh v) {
        setVar(mExportVarIdx_g_WaterMesh, v);
        mExportVar_g_WaterMesh = v;
    }

    public Mesh get_g_WaterMesh() {
        return mExportVar_g_WaterMesh;
    }

    public Script.FieldID getFieldID_g_WaterMesh() {
        return createFieldID(mExportVarIdx_g_WaterMesh, null);
    }

    private final static int mExportVarIdx_g_Constants = 14;
    private ScriptField_Constants mExportVar_g_Constants;
    public void bind_g_Constants(ScriptField_Constants v) {
        mExportVar_g_Constants = v;
        if (v == null) bindAllocation(null, mExportVarIdx_g_Constants);
        else bindAllocation(v.getAllocation(), mExportVarIdx_g_Constants);
    }

    public ScriptField_Constants get_g_Constants() {
        return mExportVar_g_Constants;
    }

    private final static int mExportVarIdx_g_PFSBackground = 15;
    private ProgramStore mExportVar_g_PFSBackground;
    public synchronized void set_g_PFSBackground(ProgramStore v) {
        setVar(mExportVarIdx_g_PFSBackground, v);
        mExportVar_g_PFSBackground = v;
    }

    public ProgramStore get_g_PFSBackground() {
        return mExportVar_g_PFSBackground;
    }

    public Script.FieldID getFieldID_g_PFSBackground() {
        return createFieldID(mExportVarIdx_g_PFSBackground, null);
    }

    private final static int mExportFuncIdx_initLeaves = 0;
    public void invoke_initLeaves() {
        invoke(mExportFuncIdx_initLeaves);
    }

    private final static int mExportFuncIdx_addDrop = 1;
    public void invoke_addDrop(int x, int y) {
        FieldPacker addDrop_fp = new FieldPacker(8);
        addDrop_fp.addI32(x);
        addDrop_fp.addI32(y);
        invoke(mExportFuncIdx_addDrop, addDrop_fp);
    }

}

