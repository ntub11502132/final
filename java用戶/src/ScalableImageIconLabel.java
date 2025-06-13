import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ScalableImageIconLabel extends JLabel {

    private ImageIcon originalIcon;

    public ScalableImageIconLabel() {
        super();
        setHorizontalAlignment(SwingConstants.CENTER); // 圖片居中
        setVerticalAlignment(SwingConstants.CENTER);
    }

    public ScalableImageIconLabel(ImageIcon icon) {
        this();
        setIcon(icon);
    }

    @Override
    public void setIcon(Icon icon) {
        if (icon instanceof ImageIcon) {
            this.originalIcon = (ImageIcon) icon;
            // 不在這裡直接縮放，而是在 paintComponent 中根據組件大小繪製
            super.setIcon(null); // 先移除舊的，讓 paintComponent 完全控制
        } else {
            this.originalIcon = null;
            super.setIcon(icon);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // 繪製背景等
        if (originalIcon != null) {
            Image image = originalIcon.getImage();
            if (image != null) {
                // 計算新的尺寸以填充整個 JLabel 區域
                int labelWidth = getWidth();
                int labelHeight = getHeight();

                // 計算圖片的原始寬高比
                double imageRatio = (double) image.getWidth(this) / image.getHeight(this);
                // 計算 JLabel 的寬高比
                double labelRatio = (double) labelWidth / labelHeight;

                int drawWidth;
                int drawHeight;

                // 根據寬高比決定是按寬度縮放還是按高度縮放
                // 目標是填充整個區域，即使裁剪或拉伸
                // 如果你想保持比例並填充，可能會留白，這就是你的「沒有占滿」問題的根源
                // 如果是「拉伸佔滿」，那就不需要考慮比例了，直接拉伸
                drawWidth = labelWidth;
                drawHeight = labelHeight;

                // 繪製縮放後的圖片
                g.drawImage(image, 0, 0, drawWidth, drawHeight, this);
            }
        }
    }

    @Override
    public Dimension getPreferredSize() {
        // 如果有原始圖片，可以根據原始圖片比例和父容器的尺寸來計算首選大小
        // 但由於我們是要「占滿」，所以這裡可能不需要嚴格的首選大小
        // 可以返回一個預設值，或讓父容器的 GridLayout 來決定
        return new Dimension(120, 180); // 這裡可以設定一個基礎的預設大小，但 GridLayout 會覆蓋它
    }
}