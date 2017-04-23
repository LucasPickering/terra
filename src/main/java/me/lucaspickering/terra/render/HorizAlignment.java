package me.lucaspickering.terra.render;

public enum HorizAlignment {

    LEFT {
        @Override
        public int leftAdjustment(int width) {
            return 0;
        }
    },
    CENTER {
        @Override
        public int leftAdjustment(int width) {
            return -width / 2;
        }
    },
    RIGHT {
        @Override
        public int leftAdjustment(int width) {
            return -width;
        }
    };

    /**
     * Provides an adjustment to be applied to a x-value of an element with this alignment,
     * dependent upon the width of the element.
     *
     * For example, a center-aligned element with width of 100 would have an adjustment of -50.
     *
     * @param width the width of the element
     * @return the amount that an element's x-value should be adjusted
     */
    public abstract int leftAdjustment(int width);
}
