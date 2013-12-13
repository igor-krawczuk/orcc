/*
 * Copyright (c) 2013, INSA of Rennes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   * Neither the name of the INSA of Rennes nor the names of its
 *     contributors may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

#include "open_hevc.h"

int openHEVCIsPresent(){
#ifdef OPEN_HEVC_ENABLE
    return 1;
#else
    return 0;
#endif
}
#define av_clip_pixel(a) av_clip_uint8(a)false

static av_clip_uint8(int a)
{
    if (a&(~0xFF)) return (-a)>>31;
    else           return a;
}

void put_hevc_qpel_pixel_orcc(i16 _dst[2][64*64], u8 listIdx,
u8 _src[71*71], u8 srcstride,
u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    u8  *src = &_src[3+3*srcstride];
    i16 *dst = _dst[listIdx];

    ff_hevc_put_hevc_qpel_pixels4_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0);
#endif
}

void put_hevc_epel_pixel_orcc(i16 _dst[2][64*64], u8 listIdx,
u8 _src[71*71], u8 srcstride,
u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    u8  *src = &_src[1+1*srcstride];
    i16 *dst = _dst[listIdx];
    ff_hevc_put_hevc_epel_pixels4_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0);
#endif
}

void put_hevc_qpel_h_orcc(i16 _dst[2][64*64], u8 listIdx,
u8 _src[71*71], u8 srcstride,
i32 filterIdx,  u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    u8  *src = &_src[3+3*srcstride];
    i16 *dst = _dst[listIdx];

    if(filterIdx == 1) {
        ff_hevc_put_hevc_qpel_h4_1_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0);
    }else if(filterIdx == 2){
        ff_hevc_put_hevc_qpel_h4_2_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0);
    }else{
        ff_hevc_put_hevc_qpel_h4_3_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0);
    }
#endif
}

void put_hevc_qpel_v_orcc(i16 _dst[2][64*64], u8 listIdx,
u8 _src[71*71], u8 srcstride,
i32 filterIdx,  u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    u8  *src = &_src[3+3*srcstride];
    i16 *dst = _dst[listIdx];

    if(filterIdx == 1) {
        ff_hevc_put_hevc_qpel_v4_1_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0);
    }else if(filterIdx == 2){
        ff_hevc_put_hevc_qpel_v4_2_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0);
    }else{
        ff_hevc_put_hevc_qpel_v4_3_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0);
    }
#endif
}

void put_hevc_epel_h_orcc(i16 _dst[2][64*64], u8 listIdx,
u8 _src[71*71], u8 srcstride,
i32 filterIdx,  u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    u8  *src = &_src[1+1*srcstride];
    i16 *dst = _dst[listIdx];

    ff_hevc_put_hevc_epel_h4_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, filterIdx, 0, 0);
#endif
}

void put_hevc_epel_v_orcc(i16 _dst[2][64*64], u8 listIdx,
u8 _src[71*71], u8 srcstride,
i32 filterIdx,  u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    u8  *src = &_src[1+1*srcstride];
    i16 *dst = _dst[listIdx];

    ff_hevc_put_hevc_epel_v4_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, 0, filterIdx, 0);
#endif
}

void put_hevc_qpel_hv_orcc(i16 _dst[2][64*64], u8 listIdx,
u8 _src[71*71], u8 srcstride,
i32 filterIdx[2],  u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    u8  *src = &_src[3+ 3*srcstride];
    i16 *dst = _dst[listIdx];
    DECLARE_ALIGNED(16, i16, mc_buffer[(64 + 7) * 64]);

    if(filterIdx[0] == 1) {
        if(filterIdx[1] == 1){
            ff_hevc_put_hevc_qpel_h4_1_v_1_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }else if(filterIdx[1] == 2){
            ff_hevc_put_hevc_qpel_h4_1_v_2_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }else{
            ff_hevc_put_hevc_qpel_h4_1_v_3_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }
    }else if(filterIdx[0] == 2){
        if(filterIdx[1] == 1){
            ff_hevc_put_hevc_qpel_h4_2_v_1_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }else if(filterIdx[1] == 2){
            ff_hevc_put_hevc_qpel_h4_2_v_2_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }else{
            ff_hevc_put_hevc_qpel_h4_2_v_3_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }
    }else{
        if(filterIdx[1] == 1){
            ff_hevc_put_hevc_qpel_h4_3_v_1_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }else if(filterIdx[1] == 2){
            ff_hevc_put_hevc_qpel_h4_3_v_2_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }else{
            ff_hevc_put_hevc_qpel_h4_3_v_3_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, &mc_buffer);
        }
    }
#endif
}

void put_hevc_epel_hv_orcc(i16 _dst[2][64*64], u8 listIdx,
u8 _src[71*71], u8 srcstride,
i32 filterIdx[2],  u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    u8  *src = &_src[1+1*srcstride];
    i16 *dst = _dst[listIdx];
    DECLARE_ALIGNED(16, i16, mc_buffer[(64 + 7) * 64]);

    ff_hevc_put_hevc_epel_hv4_8_sse(dst, width + 1, src, srcstride, width + 1, height + 1, filterIdx[0], filterIdx[1], &mc_buffer);
#endif
}

void put_unweighted_pred_orcc(u8 _dst[2][64*64], i16 _src[2][64*64], u8 width, u8 height, u8 rdList)
{
#ifdef OPEN_HEVC_ENABLE
    i16 *src = &_src;
    u8 *dst = _dst[rdList];

    ff_hevc_put_unweighted_pred_8_sse(dst, width + 1, src, width + 1, width + 1, height + 1);
#endif
}


void put_unweighted_pred_avg_orcc(u8 _dst[2][64*64], i16 _src[2][64*64], u8 width, u8 height)
{
#ifdef OPEN_HEVC_ENABLE
    i16 *src1 = &_src[0];
    i16 *src2 = &_src[1];
    u8 *dst = _dst[0];

    ff_hevc_put_weighted_pred_avg_8_sse(dst, width + 1, src1, src2, width + 1, width + 1, height + 1);
#endif
}
