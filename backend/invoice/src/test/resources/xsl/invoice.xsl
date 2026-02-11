<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:template match="/">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master
                        master-name="simple"
                        page-height="11in"
                        page-width="8.5in"
                        margin="1in">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="simple">
                <fo:flow flow-name="xsl-region-body">
                    <fo:block>
                        Test Invoice PDF
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

</xsl:stylesheet>
