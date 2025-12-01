package com.block.chainsaw.contract.IPFS;

public record IpfsDTO (
    String filePath,
    String fileHash,
    String fileName,
    Long fileSize
){}
