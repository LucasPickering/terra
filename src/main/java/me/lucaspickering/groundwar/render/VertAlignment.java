package me.lucaspickering.groundwar.render;

public enum VertAlignment {

    TOP {
        @Override
        public int topAdjustment(int height) {
            return 0;
        }
    },
    CENTER {
        @Override
        public int topAdjustment(int height) {
            return -height / 2;
        }
    },
    BOTTOM {
        @Override
        public int topAdjustment(int height) {
            return -height;
        }
    };

    /**
     * Provides an adjustment to be applied to a y-value of an element with this alignment,
     * dependent upon the height of the element.
     *
     * For example, a center-aligned element with height of 100 would have an adjustment of 50.
     *
     * @param height the height of the element
     * @return the amount that an element's y-value should be adjusted
     */
    public abstract int topAdjustment(int height);
}
