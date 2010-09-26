<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml" encoding="utf-8" indent="yes" />
  <xsl:strip-space elements="*" />

  <xsl:template match="*" name="copy">

    <xsl:variable name="currentParent" select="@parent" />
    <xsl:variable name="currentOrder" select="number(@order)" />

    <xsl:variable name="siblingCount"
      select="count(//menu[@parent=$currentParent])" />
    <xsl:variable name="precedingCount"
      select="count(//menu[@parent=$currentParent and number(@order) &lt; $currentOrder])" />
    <xsl:variable name="followindCount"
      select="$siblingCount - $precedingCount - 1" />

    <xsl:variable name="newParent">
      <xsl:choose>
        <xsl:when test="$precedingCount >= 20 and $followindCount &lt; 10">
          <xsl:value-of
            select="concat($currentParent, '_', floor(($siblingCount - 1) div 10) - 1)" />
        </xsl:when>
        <xsl:when test="$precedingCount >= 10 and $siblingCount > 20">
          <xsl:value-of
            select="concat($currentParent, '_', floor($precedingCount div 10))" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$currentParent" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if
      test="$precedingCount > 0 and $precedingCount mod 10 = 0 and $followindCount >= 10">
      <xsl:element name="menu">
        <xsl:attribute name="id">
          <xsl:value-of select="$newParent" />
        </xsl:attribute>
        <xsl:attribute name="text">
          <xsl:value-of select="concat('sub ', $newParent)" />
        </xsl:attribute>

        <xsl:attribute name="type">S</xsl:attribute>
        <xsl:attribute name="style">SUB</xsl:attribute>

        <xsl:for-each select="@parent | @order | @separators | @visible">
          <xsl:attribute name="{name(.)}">
            <xsl:value-of select="." />
          </xsl:attribute>
        </xsl:for-each>
      </xsl:element>
    </xsl:if>

    <xsl:if test="name() = 'menu'">
      <xsl:copy>
        <xsl:copy-of select="@*" />

        <xsl:attribute name="pos">
          <xsl:value-of
          select="concat($precedingCount + 1, '/', $siblingCount)" />
        </xsl:attribute>

        <xsl:attribute name="parent">
          <xsl:value-of select="$newParent" />
        </xsl:attribute>

        <xsl:if test="starts-with(@service, 'g_o_')">
          <xsl:variable name="serviceParameter">
            <xsl:value-of select="substring-before(substring-after(@service, &quot;&apos;&quot;), &quot;&apos;&quot;)" />
          </xsl:variable>
          <xsl:if test="$serviceParameter != ''">
            <xsl:attribute name="parameters">
              <xsl:value-of select="$serviceParameter"/>
            </xsl:attribute>
          </xsl:if>
          
          <xsl:attribute name="service">
            <xsl:choose>
              <xsl:when test="contains(@service, 'open_grid')">
                <xsl:value-of select="'grid'"/>
              </xsl:when>
              <xsl:when test="contains(@service, 'open_form')">
                <xsl:value-of select="'form'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="substring-after(@service, 'g_o_')"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </xsl:if>

      </xsl:copy>
    </xsl:if>

    <xsl:apply-templates>
      <xsl:sort select="@parent" />
      <xsl:sort select="@order" data-type="number" />
    </xsl:apply-templates>

  </xsl:template>

  <xsl:template match="menu[@type='P']">
    <xsl:copy-of select="." />
  </xsl:template>

</xsl:stylesheet>