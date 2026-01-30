<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:output method="xml" indent="yes"/>

    <!-- Root template -->
    <xsl:template match="/invoice">

        <fo:root>

            <!-- Page layout -->
            <fo:layout-master-set>
                <fo:simple-page-master
                        master-name="A4"
                        page-height="29.7cm"
                        page-width="21cm"
                        margin="2cm">

                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>

            <fo:page-sequence master-reference="A4">
                <fo:flow flow-name="xsl-region-body">

                    <!-- Title -->
                    <fo:block font-size="18pt"
                              font-weight="bold"
                              text-align="center"
                              space-after="10pt">
                        Invoice
                    </fo:block>

                    <!-- Invoice meta -->
                    <fo:block font-size="10pt" space-after="4pt">
                        <fo:inline font-weight="bold">Invoice Number: </fo:inline>
                        <xsl:value-of select="invoiceNumber"/>
                    </fo:block>

                    <fo:block font-size="10pt" space-after="4pt">
                        <fo:inline font-weight="bold">Invoice Date: </fo:inline>
                        <xsl:value-of select="invoiceDate"/>
                    </fo:block>

                    <fo:block font-size="10pt" space-after="10pt">
                        <fo:inline font-weight="bold">Client: </fo:inline>
                        <xsl:value-of select="clientName"/>
                    </fo:block>

                    <!-- Items table -->
                    <fo:table table-layout="fixed" width="100%" border="1pt solid black">

                        <!-- Table columns -->
                        <fo:table-column column-width="40%"/>
                        <fo:table-column column-width="15%"/>
                        <fo:table-column column-width="20%"/>
                        <fo:table-column column-width="25%"/>

                        <!-- Table header -->
                        <fo:table-header>
                            <fo:table-row font-weight="bold" background-color="#EEEEEE">
                                <fo:table-cell padding="4pt">
                                    <fo:block>Product</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="4pt">
                                    <fo:block>Qty</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="4pt">
                                    <fo:block>Price</fo:block>
                                </fo:table-cell>
                                <fo:table-cell padding="4pt">
                                    <fo:block>Total</fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-header>

                        <!-- Table body -->
                        <fo:table-body>
                            <xsl:for-each select="items/item">
                                <fo:table-row>
                                    <fo:table-cell padding="4pt">
                                        <fo:block>
                                            <xsl:value-of select="productName"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="4pt">
                                        <fo:block>
                                            <xsl:value-of select="quantity"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="4pt">
                                        <fo:block>
                                            <xsl:value-of select="sellingPrice"/>
                                        </fo:block>
                                    </fo:table-cell>
                                    <fo:table-cell padding="4pt">
                                        <fo:block>
                                            <xsl:value-of select="lineTotal"/>
                                        </fo:block>
                                    </fo:table-cell>
                                </fo:table-row>
                            </xsl:for-each>
                        </fo:table-body>

                    </fo:table>

                    <!-- Total amount -->
                    <fo:block text-align="right"
                              font-size="12pt"
                              font-weight="bold"
                              space-before="10pt">
                        Total Amount:
                        <xsl:text> </xsl:text>
                        <xsl:value-of select="totalAmount"/>
                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>

    </xsl:template>

</xsl:stylesheet>
