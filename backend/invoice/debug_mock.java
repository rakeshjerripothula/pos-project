import com.increff.invoice.model.internal.InvoiceModel;
import com.increff.invoice.util.XmlBuilderUtil;
import java.util.Collections;

public class debug_mock {
    public static void main(String[] args) {
        // Test if mock injection works
        XmlBuilderUtil mock = org.mockito.Mockito.mock(XmlBuilderUtil.class);
        org.mockito.Mockito.when(mock.buildInvoiceXml(org.mockito.ArgumentMatchers.any()))
            .thenReturn("<test>xml</test>");
        
        InvoiceModel model = new InvoiceModel();
        model.setItems(Collections.emptyList());
        
        String result = mock.buildInvoiceXml(model);
        System.out.println("Mock result: " + result);
        System.out.println("Mock working: " + result.equals("<test>xml</test>"));
    }
}
